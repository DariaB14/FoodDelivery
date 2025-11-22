package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.CartMapper;
import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.response.CartItemResponse;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.entity.*;
import com.example.fooddelivery.exception.BusinessException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.repository.CartItemRepository;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.CourierRepository;
import com.example.fooddelivery.repository.ItemOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemOptionRepository itemOptionRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CourierRepository courierRepository;

    @InjectMocks
    private CartService cartService;

    private final Long USER_ID = 1L;
    private final Long CART_ID = 1L;
    private final Long ITEM_ID = 1L;
    private final Long ITEM_OPTION_ID = 10L;
    private final Long RESTAURANT_ID = 5L;
    private final Long NON_EXISTENT_USER_ID = 999L;
    private final Long NON_EXISTENT_ITEM_ID = 999L;

    private CartItemRequest cartItemRequest;
    private CartItemResponse cartItemResponse;
    private CartResponse cartResponse;
    private Cart cart;
    private User user;
    private Restaurant restaurant;
    private ItemOption itemOption;
    private Item item;

    @BeforeEach
    void setUp() {
        cartItemRequest = new CartItemRequest(ITEM_ID, ITEM_OPTION_ID, 3);
        cartItemResponse = new CartItemResponse(5L, ITEM_ID, ITEM_OPTION_ID, 3);
        cartResponse = new CartResponse(CART_ID, USER_ID, RESTAURANT_ID, List.of(cartItemResponse), 45);

        user = new User();
        user.setId(USER_ID);

        restaurant = new Restaurant();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setActive(true);
        restaurant.setOpeningTime(LocalTime.of(10, 0));
        restaurant.setClosingTime(LocalTime.of(22, 0));

        item = new Item();
        item.setId(ITEM_ID);
        item.setAvailable(true);
        item.setRestaurant(restaurant);

        itemOption = new ItemOption();
        itemOption.setId(ITEM_OPTION_ID);
        itemOption.setItem(item);
        itemOption.setPreparationMinutes(20);

        cart = new Cart();
        cart.setId(CART_ID);
        cart.setUser(user);
        cart.setRestaurant(restaurant);
        cart.setItems(new ArrayList<>());
    }

    @Test
    void addItem_Success() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(ITEM_OPTION_ID)).thenReturn(Optional.of(itemOption));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toDto(cart)).thenReturn(cartResponse);

        CartResponse result = cartService.addItem(USER_ID, cartItemRequest);

        assertThat(result).isNotNull();

        verify(cartRepository).save(cart);
    }

    @Test
    void addItemWhenItemOptionNotFound() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(ITEM_OPTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.addItem(USER_ID, cartItemRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Item option with id 10 not found");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemWhenItemNotAvailable() {
        item.setAvailable(false);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(ITEM_OPTION_ID)).thenReturn(Optional.of(itemOption));

        assertThatThrownBy(() -> cartService.addItem(USER_ID, cartItemRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Item is not available now");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemWhenRestaurantClosed() {
        restaurant.setActive(false);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(ITEM_OPTION_ID)).thenReturn(Optional.of(itemOption));

        assertThatThrownBy(() -> cartService.addItem(USER_ID, cartItemRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Restaurant is closed");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void addItemWhenDifferentRestaurant() {
        Restaurant differentRestaurant = new Restaurant();
        differentRestaurant.setId(2L);
        cart.setRestaurant(differentRestaurant);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(itemOptionRepository.findById(ITEM_OPTION_ID)).thenReturn(Optional.of(itemOption));

        assertThatThrownBy(() -> cartService.addItem(USER_ID, cartItemRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Must not add item from another restaurant");

        verify(cartRepository, never()).save(any());
    }

    @Test
    void deleteItem_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(ITEM_ID);
        cartItem.setCart(cart);
        cart.getItems().add(cartItem);

        when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(cartItem));

        cartService.deleteItem(USER_ID, ITEM_ID);

        verify(cartItemRepository).delete(cartItem);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void deleteItemWhenItemNotFound() {
        when(cartItemRepository.findById(NON_EXISTENT_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.deleteItem(USER_ID, NON_EXISTENT_ITEM_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cart item with id 999 not found");

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void updateQuantity_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(ITEM_ID);
        cartItem.setQuantity(2);

        when(cartItemRepository.findById(ITEM_ID)).thenReturn(Optional.of(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(cartItem);
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartResponse);

        CartResponse result = cartService.updateQuantity(USER_ID, ITEM_ID, 5);

        assertThat(result).isNotNull();

        verify(cartItemRepository).save(cartItem);
    }

    @Test
    void updateQuantityWhenItemNotFound() {
        when(cartItemRepository.findById(NON_EXISTENT_ITEM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.updateQuantity(USER_ID, NON_EXISTENT_ITEM_ID, 5))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cart item with id 999 not found");

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void getCart_Success() {
        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
        when(cartMapper.toDto(cart)).thenReturn(cartResponse);

        CartResponse result = cartService.getCart(USER_ID);

        assertThat(result).isNotNull();

        verify(cartRepository).findByUserId(USER_ID);
    }

    @Test
    void getCartWhenCartNotFound() {
        when(cartRepository.findByUserId(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.getCart(NON_EXISTENT_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cart for user with id 999 not found");
    }

    @Test
    void clearCart_Success() {
        CartItem cartItem = new CartItem();
        cartItem.setId(ITEM_ID);
        cart.getItems().add(cartItem);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

        cartService.clearCart(USER_ID);

        assertThat(cart.getItems()).isEmpty();
        assertThat(cart.getRestaurant()).isNull();

        verify(cartRepository).save(cart);
    }

    @Test
    void clearCartWhenCartNotFound() {
        when(cartRepository.findByUserId(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.clearCart(NON_EXISTENT_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cart for user with id 999 not found");

        verify(cartRepository, never()).save(any());
    }
}