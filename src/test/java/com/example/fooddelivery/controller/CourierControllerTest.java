package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CourierRequest;
import com.example.fooddelivery.dto.response.CourierResponse;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.enums.CourierStatus;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.exception.BusinessException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.service.CourierService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CourierController.class)
class CourierControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CourierService courierService;

    private CourierRequest courierRequest;
    private CourierResponse courierResponse;

    private static final String BASE_URL = "/couriers";

    @BeforeEach
    void setUp() {
        courierRequest = new CourierRequest("Max", "+79991234567");
        courierResponse = new CourierResponse(1L, "Max", "+79991234567", CourierStatus.FREE, new BigDecimal("4.5"), 1);
    }

    @Test
    void registerCourierWithValidData_Success() throws Exception {
        when(courierService.registerCourier(any(CourierRequest.class))).thenReturn(courierResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courierRequest)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("invalidCourierData")
    void registerCourierWithInvalidData_ShouldReturnBadRequest(String testName, CourierRequest invalidRequest) throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidCourierData() {
        return Stream.of(
                Arguments.of("emptyName",
                        new CourierRequest("", "+79991234567")),
                Arguments.of("nullName",
                        new CourierRequest(null, "+79991234567")),
                Arguments.of("invalidPhone",
                        new CourierRequest("Max", "79991234567"))
        );
    }

    @Test
    void getAllCouriers_Success() throws Exception {
        List<CourierResponse> couriers = List.of(
                new CourierResponse(1L, "Max", "+79991234567", CourierStatus.FREE, new BigDecimal("4.5"), 1),
                new CourierResponse(2L, "Max", "+79997654321", CourierStatus.OFFLINE, new BigDecimal("4.3"), 0)
        );

        when(courierService.getAllCouriers()).thenReturn(couriers);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void assignOrder_Success() throws Exception {
        Long courierId = 1L;
        Long orderId = 100L;

        CourierResponse updatedCourier = new CourierResponse(courierId, "Max", "+79991234567",
                CourierStatus.FREE, new BigDecimal("4.5"), 2);

        when(courierService.assignOrder(eq(courierId), eq(orderId))).thenReturn(updatedCourier);

        mockMvc.perform(patch(BASE_URL + "/{id}/assign", courierId)
                        .param("orderId", orderId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void assignOrderWhenCourierNotFound() throws Exception {
        Long courierId = 200L;
        Long orderId = 100L;

        when(courierService.assignOrder(eq(courierId), eq(orderId)))
                .thenThrow(new EntityNotFoundException("Courier with id 200 not found"));

        mockMvc.perform(patch(BASE_URL + "/{id}/assign", courierId)
                        .param("orderId", orderId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void assignOrderWhenCourierIsOffline() throws Exception {
        Long courierId = 1L;
        Long orderId = 100L;

        when(courierService.assignOrder(eq(courierId), eq(orderId)))
                .thenThrow(new BusinessException("Courier must be ONLINE to accept orders"));

        mockMvc.perform(patch(BASE_URL + "/{id}/assign", courierId)
                        .param("orderId", orderId.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void assignOrderWithoutOrderIdParam() throws Exception {
        Long courierId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/assign", courierId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getActiveOrders_Success() throws Exception {
        Long courierId = 1L;

        OrderResponse order1 = new OrderResponse(100L, 1L, 50L, OrderStatus.TAKED, new BigDecimal("2500.00"));
        OrderResponse order2 = new OrderResponse(150L, 2L, 55L, OrderStatus.TAKED, new BigDecimal("1900.00"));

        List<OrderResponse> activeOrders = List.of(order1, order2);

        when(courierService.getActiveOrders(courierId)).thenReturn(activeOrders);

        mockMvc.perform(get(BASE_URL + "/{id}/orders", courierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getActiveOrdersWhenCourierNotFound() throws Exception {
        Long courierId = 100L;

        when(courierService.getActiveOrders(courierId)).thenThrow(new EntityNotFoundException("Courier with id 100 not found"));

        mockMvc.perform(get(BASE_URL + "/{id}/orders", courierId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_Success() throws Exception {
        Long courierId = 1L;
        CourierStatus newStatus = CourierStatus.OFFLINE;

        CourierResponse updatedCourier = new CourierResponse(courierId, "Max", "+79991222556", newStatus, new BigDecimal("4.5"), 0);

        when(courierService.updateStatus(eq(courierId), eq(newStatus))).thenReturn(updatedCourier);

        mockMvc.perform(patch(BASE_URL + "/{id}/status", courierId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatusWhenCourierNotFound() throws Exception {
        Long courierId = 100L;
        CourierStatus newStatus = CourierStatus.FREE;

        when(courierService.updateStatus(eq(courierId), eq(newStatus))).thenThrow(new EntityNotFoundException("Courier with id 100 not found"));

        mockMvc.perform(patch(BASE_URL + "/{id}/status", courierId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusWhenCourierHasOrders() throws Exception {
        Long courierId = 1L;
        CourierStatus newStatus = CourierStatus.OFFLINE;

        when(courierService.updateStatus(eq(courierId), eq(newStatus))).thenThrow(new BusinessException("Cannot go offline with active orders"));

        mockMvc.perform(patch(BASE_URL + "/{id}/status", courierId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isConflict());
    }

    @Test
    void updateStatusWithoutStatusParam() throws Exception {
        Long courierId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/status", courierId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusWithInvalidStatus() throws Exception {
        Long courierId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/status", courierId)
                        .param("status", "INVALID_STATUS"))
                .andExpect(status().isBadRequest());
    }
}