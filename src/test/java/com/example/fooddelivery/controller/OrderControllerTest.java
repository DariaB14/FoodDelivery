package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.exception.AccessDeniedException;
import com.example.fooddelivery.exception.BusinessException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.exception.StatusException;
import com.example.fooddelivery.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;
    private static final String BASE_URL = "/orders";

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest(1L);
        orderResponse = new OrderResponse(1L, 5L, 10L, OrderStatus.NEW, new BigDecimal("500.00"));
    }

    @Test
    void createOrder_Success() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(orderResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrderWithNullCartId() throws Exception {
        OrderRequest invalidRequest = new OrderRequest(null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrderWhenCartNotFound() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new EntityNotFoundException("Cart with id 100 not found"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createOrderWhenCartEmpty() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new BusinessException("Order cannot be created without items"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void createOrderWhenAmountIsSmallerThan300() throws Exception {
        when(orderService.createOrder(any(OrderRequest.class)))
                .thenThrow(new BusinessException("Order cannot be created with total amount smaller than 300.00"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getOrder_Success() throws Exception {
        Long orderId = 1L;
        when(orderService.getOrderById(orderId)).thenReturn(orderResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    void getOrderWhenOrderNotFound() throws Exception {
        Long orderId = 100L;
        when(orderService.getOrderById(orderId))
                .thenThrow(new EntityNotFoundException("Order with id 100 not found"));

        mockMvc.perform(get(BASE_URL + "/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByUserId() throws Exception {
        Long userId = 1L;
        List<OrderResponse> orders = List.of(
                new OrderResponse(1L, userId, 1L, OrderStatus.NEW, new BigDecimal("500.00")),
                new OrderResponse(2L, userId, 1L, OrderStatus.CONFIRMED, new BigDecimal("550.00"))
        );

        when(orderService.getOrdersById(userId)).thenReturn(orders);

        mockMvc.perform(get(BASE_URL)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOrdersByStatus() throws Exception {
        OrderStatus status = OrderStatus.READY;
        List<OrderResponse> orders = List.of(
                new OrderResponse(1L, 1L, 1L, status, new BigDecimal("500.00")),
                new OrderResponse(2L, 2L, 2L, status, new BigDecimal("1000.00"))
        );

        when(orderService.getOrdersByStatus(status)).thenReturn(orders);

        mockMvc.perform(get(BASE_URL)
                        .param("status", status.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateStatus_Success() throws Exception {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;
        OrderResponse updatedResponse = new OrderResponse(orderId, 1L, 1L, newStatus, new BigDecimal("500.00"));

        when(orderService.updateStatus(orderId, newStatus)).thenReturn(updatedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatusWhenOrderNotFound() throws Exception {
        Long orderId = 100L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;

        when(orderService.updateStatus(orderId, newStatus))
                .thenThrow(new EntityNotFoundException("Order with id 100 not found"));

        mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusWhenSOrderDelievered() throws Exception {
        Long orderId = 1L;
        OrderStatus newStatus = OrderStatus.CONFIRMED;

        when(orderService.updateStatus(orderId, newStatus))
                .thenThrow(new StatusException("Delivered order's status cannot be updated"));

        mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void updateStatusByCourier() throws Exception {
        Long orderId = 1L;
        Long courierId = 5L;
        OrderStatus newStatus = OrderStatus.TAKED;
        OrderResponse updatedResponse = new OrderResponse(orderId, 1L, 1L, newStatus, new BigDecimal("500.00"));

        when(orderService.updateStatusByCourier(orderId, newStatus, courierId)).thenReturn(updatedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId)
                        .param("status", newStatus.toString())
                        .param("courierId", courierId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatusByCourierNotAssign() throws Exception {
        Long orderId = 1L;
        Long courierId = 5L;
        OrderStatus newStatus = OrderStatus.TAKED;

        when(orderService.updateStatusByCourier(orderId, newStatus, courierId))
                .thenThrow(new AccessDeniedException("This order belongs to another courier"));

        mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId)
                        .param("status", newStatus.toString())
                        .param("courierId", courierId.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatusWithoutParam() throws Exception {
        Long orderId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/status", orderId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelOrder_Success() throws Exception {
        Long orderId = 1L;
        doNothing().when(orderService).cancelOrder(orderId);

        mockMvc.perform(delete(BASE_URL + "/{id}", orderId))
                .andExpect(status().isNoContent());

        verify(orderService).cancelOrder(orderId);
    }

    @Test
    void cancelOrderWhenOrderNotFound() throws Exception {
        Long orderId = 100L;
        doThrow(new EntityNotFoundException("Order with id 100 not found")).when(orderService).cancelOrder(orderId);

        mockMvc.perform(delete(BASE_URL + "/{id}", orderId))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelOrder_WhenStatusException() throws Exception {
        Long orderId = 1L;
        doThrow(new StatusException("Delivered order cannot be cancelled")).when(orderService).cancelOrder(orderId);

        mockMvc.perform(delete(BASE_URL + "/{id}", orderId))
                .andExpect(status().isConflict());
    }
}