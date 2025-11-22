package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.CartMapper;
import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.entity.*;
import com.example.fooddelivery.exception.exceptions.BusinessException;
import com.example.fooddelivery.exception.exceptions.EntityNotFoundException;
import com.example.fooddelivery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final CartMapper cartMapper;
    private final CourierRepository courierRepository;

    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = getCartOrCreate(userId);

        ItemOption itemOption = itemOptionRepository.findById(request.getItemOptionId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Item option with id %d not found", request.getItemOptionId())));

        validateAddingItem(itemOption, cart);

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getItemOption().getId().equals(itemOption.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setItem(itemOption.getItem());
            newItem.setItemOption(itemOption);
            newItem.setQuantity(request.getQuantity());
            cart.getItems().add(newItem);
        }

        cartRepository.save(cart);
        return addEtaToResponse(cartMapper.toDto(cart), cart);
    }

    private void validateAddingItem(ItemOption itemOption, Cart cart){
        if (!itemOption.getItem().isAvailable()) {
            throw new BusinessException("Item is not available now");
        }

        Restaurant restaurant = itemOption.getItem().getRestaurant();
        if (!restaurant.isActive()) {
            throw new BusinessException("Restaurant is closed");
        }

        if (!isRestaurantOpen(itemOption.getItem().getRestaurant())) {
            throw new BusinessException("Restaurant is closed");
        }

        if (cart.getRestaurant() == null) {
            cart.setRestaurant(itemOption.getItem().getRestaurant());
        } else if (!cart.getRestaurant().getId().equals(itemOption.getItem().getRestaurant().getId())) {
            throw new BusinessException("Must not add item from another restaurant");
        }
    }

    public void deleteItem(Long userId, Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart item with id %d not found", itemId)));

        cartItemRepository.delete(cartItem);

        Cart cart = cartItem.getCart();
        if (cart.getItems().isEmpty()) {
            cart.setRestaurant(null);
            cartRepository.save(cart);
        }
    }

    public CartResponse updateQuantity(Long userId, Long itemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart item with id %d not found", itemId)));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return getCart(userId);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart for user with id %d not found", userId)));

        return addEtaToResponse(cartMapper.toDto(cart), cart);
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart for user with id %d not found", userId)));

        cart.getItems().clear();
        cart.setRestaurant(null);
        cartRepository.save(cart);
    }

    private Cart getCartOrCreate(Long userId){
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    User user = new User();
                    user.setId(userId);
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    private boolean isRestaurantOpen(Restaurant restaurant) {
        LocalTime now = LocalTime.now();
        return !now.isBefore(restaurant.getOpeningTime()) && now.isBefore(restaurant.getClosingTime());
    }

    private CartResponse addEtaToResponse(CartResponse response, Cart cart) {
        Integer eta = calculateEta(cart);
        return new CartResponse(
                response.id(),
                response.userId(),
                response.restaurantId(),
                response.items(),
                eta
        );
    }

    private Integer calculateEta(Cart cart){
        if(cart.getRestaurant()==null || cart.getItems().isEmpty()){
            return null;
        }

        Integer preparationTime = calculatePrepTime(cart);
        Integer deliveryTime = calculateDeliveryTime();
        return preparationTime+deliveryTime;
    }

    private Integer calculatePrepTime(Cart cart) {
        return cart.getItems().stream()
                .mapToInt(item -> item.getItemOption().getPreparationMinutes())
                .max()
                .orElse(0);
    }

    private Integer calculateDeliveryTime() {
        Integer baseDeliveryTime = 30;
        if (isEvening()) baseDeliveryTime += 5;
        if (isWeekend()) baseDeliveryTime += 10;
        baseDeliveryTime += getCourierLoad();
        return baseDeliveryTime;
    }

    private boolean isEvening() {
        LocalTime now = LocalTime.now();
        LocalTime eveningStart = LocalTime.of(18, 0);
        LocalTime eveningEnd = LocalTime.of(22, 0);

        return !now.isBefore(eveningStart) && now.isBefore(eveningEnd);
    }

    private boolean isWeekend() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
    }

    private Integer getCourierLoad() {
        List<Courier> allCouriers = courierRepository.findAll();
        long busyCouriers = allCouriers.stream()
                .filter(courier -> courier.getCurrentOrdersAmount() > 0)
                .count();

        if (allCouriers.isEmpty()) {
            return 0;
        }

        double load = (double) busyCouriers / allCouriers.size();

        if (load > 0.8) return 15;
        else if (load > 0.5) return 10;
        else return 0;
    }
}
