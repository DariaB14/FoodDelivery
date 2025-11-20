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

import java.time.LocalTime;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ItemOptionRepository itemOptionRepository;
    private final CartMapper cartMapper;

    public CartResponse addItem(CartItemRequest request) {
        Long userId = getAuthenticatedOwnerId();
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
        if (!isRestaurantOpen(itemOption.getItem().getRestaurant())) {
            throw new BusinessException("Restaurant is closed");
        }

        if (cart.getRestaurant() == null) {
            cart.setRestaurant(itemOption.getItem().getRestaurant());
        } else if (!cart.getRestaurant().getId().equals(itemOption.getItem().getRestaurant().getId())) {
            throw new BusinessException("Must not add item from another restaurant");
        }
    }

    public void deleteItem(Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart item with id %d not found", itemId)));

        cartItemRepository.delete(cartItem);

        Cart cart = cartItem.getCart();
        if (cart.getItems().isEmpty()) {
            cart.setRestaurant(null);
            cartRepository.save(cart);
        }
    }

    public CartResponse updateQuantity(Long itemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart item with id %d not found", itemId)));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        return getCart();
    }

    @Transactional(readOnly = true)
    public CartResponse getCart() {
        Long userId = getAuthenticatedOwnerId();

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart for user with id %d not found", userId)));

        return addEtaToResponse(cartMapper.toDto(cart), cart);
    }

    public void clearCart() {
        Long userId = getAuthenticatedOwnerId();
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart for user with id %d not found", userId)));

        cart.getItems().clear();
        cart.setRestaurant(null);
        cartRepository.save(cart);
    }

    // На данный момент возвращаем заглушку, при подключении Spring Security будем проверять через
    // SecurityContextHolder.getContext().getAuthentication()
    private Long getAuthenticatedOwnerId(){
        return 1L;
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
        return null;
    }
}
