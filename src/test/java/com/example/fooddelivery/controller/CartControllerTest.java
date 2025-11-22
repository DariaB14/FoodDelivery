package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.response.CartItemResponse;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.exception.BusinessException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
class CartControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    private CartItemRequest cartItemRequest;
    private CartItemResponse cartItemResponse;
    private CartResponse cartResponse;

    private static final String BASE_URL = "/cart";

    @BeforeEach
    void setUp() {
        cartItemRequest = new CartItemRequest(1L, 10L, 3);
        cartItemResponse = new CartItemResponse(5L, 1L, 10L, 3);
        cartResponse = new CartResponse(1L, 1L, 5L, List.of(cartItemResponse), 45);
    }

    @Test
    void addItemWithValidData_Success() throws Exception {
        when(cartService.addItem(anyLong(), any(CartItemRequest.class))).thenReturn(cartResponse);

        mockMvc.perform(post(BASE_URL + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void addItemWhenItemOptionNotFound() throws Exception {
        when(cartService.addItem(anyLong(), any(CartItemRequest.class)))
                .thenThrow(new EntityNotFoundException("Item option with id 10 not found"));

        mockMvc.perform(post(BASE_URL + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @MethodSource("invalidCartItemData")
    void addItemWithInvalidData(String testName, CartItemRequest invalidRequest) throws Exception {
        mockMvc.perform(post(BASE_URL + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidCartItemData() {
        return Stream.of(
                Arguments.of("nullItemId",
                        new CartItemRequest(null, 10L, 2)),
                Arguments.of("nullItemOptionId",
                        new CartItemRequest(1L, null, 2)),
                Arguments.of("nullQuantity",
                        new CartItemRequest(1L, 10L, null)),
                Arguments.of("negativeQuantity",
                        new CartItemRequest(1L, 10L, -1)));
    }

    @Test
    void addItemWhenRestaurantClosed() throws Exception {
        when(cartService.addItem(anyLong(), any(CartItemRequest.class)))
                .thenThrow(new BusinessException("Restaurant is closed"));

        mockMvc.perform(post(BASE_URL + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartItemRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteItem_Success() throws Exception {
        Long itemId = 5L;
        doNothing().when(cartService).deleteItem(anyLong(), eq(itemId));

        mockMvc.perform(delete(BASE_URL + "/items/{id}", itemId))
                .andExpect(status().isNoContent());

        verify(cartService).deleteItem(anyLong(), eq(itemId));
    }

    @Test
    void deleteItemWhenItemNotFound() throws Exception {
        Long itemId = 100L;
        doThrow(new EntityNotFoundException("Cart item with id 100 not found"))
                .when(cartService).deleteItem(anyLong(), eq(itemId));

        mockMvc.perform(delete(BASE_URL + "/items/{id}", itemId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateQuantity_Success() throws Exception {
        Long itemId = 5L;
        Integer quantity = 5;

        when(cartService.updateQuantity(anyLong(), eq(itemId), eq(quantity))).thenReturn(cartResponse);

        mockMvc.perform(put(BASE_URL + "/items/{id}", itemId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isOk());
    }

    @Test
    void updateQuantityWithInvalidData() throws Exception {
        Long itemId = 5L;

        mockMvc.perform(put(BASE_URL + "/items/{id}", itemId)
                        .param("quantity", String.valueOf(-19)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateQuantityWhenItemNotFound() throws Exception {
        Long itemId = 100L;
        Integer quantity = 2;

        when(cartService.updateQuantity(anyLong(), eq(itemId), eq(quantity)))
                .thenThrow(new EntityNotFoundException("Cart item with id 100 not found"));

        mockMvc.perform(put(BASE_URL + "/items/{id}", itemId)
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCart_Success() throws Exception {
        when(cartService.getCart(anyLong())).thenReturn(cartResponse);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk());
    }

    @Test
    void getCartWhenCartNotFound() throws Exception {
        when(cartService.getCart(anyLong())).thenThrow(new EntityNotFoundException("Cart for user with id 1 not found"));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearCart_Success() throws Exception {
        doNothing().when(cartService).clearCart(anyLong());

        mockMvc.perform(delete(BASE_URL))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart(anyLong());
    }

    @Test
    void clearCartWhenCartNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Cart for user with id 1 not found")).when(cartService).clearCart(anyLong());

        mockMvc.perform(delete(BASE_URL))
                .andExpect(status().isNotFound());
    }
}