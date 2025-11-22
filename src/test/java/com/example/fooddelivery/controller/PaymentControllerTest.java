package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.PaymentRequest;
import com.example.fooddelivery.dto.response.PaymentResponse;
import com.example.fooddelivery.enums.PaymentStatus;
import com.example.fooddelivery.enums.PaymentType;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.exception.PaymentException;
import com.example.fooddelivery.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;

    private static final String BASE_URL = "/payments";

    @BeforeEach
    void setUp(){
        paymentRequest = new PaymentRequest(1L, PaymentType.CARD);
        paymentResponse = new PaymentResponse(1L, 1L, PaymentStatus.PENDING, PaymentType.CARD);
    }

    @Test
    void createPayment_Success() throws Exception{
        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(paymentResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isCreated());

    }

    @Test
    void createPaymentWithInvalidOrderId() throws Exception{
        PaymentRequest paymentRequest = new PaymentRequest(null, PaymentType.CARD);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());

    }

    @Test
    void createPaymentWithInvalidPaymentType() throws Exception{
        PaymentRequest paymentRequest = new PaymentRequest(1L, null);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPaymentWhenOrderNotFound() throws Exception{
        when(paymentService.createPayment(any(PaymentRequest.class))).thenThrow(new EntityNotFoundException("Order with id 100 not found"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPaymentWhenOrderAlreadyPaid() throws Exception {
        when(paymentService.createPayment(any(PaymentRequest.class))).thenThrow(new PaymentException("Order with id 1 already is payed"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPayment_Success() throws Exception {
        Long paymentId = 1L;
        when(paymentService.getPayment(paymentId)).thenReturn(paymentResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", paymentId))
                .andExpect(status().isOk());
    }

    @Test
    void getPaymentWhenPaymentNotFound() throws Exception {
        Long paymentId = 100L;
        when(paymentService.getPayment(paymentId)).thenThrow(new EntityNotFoundException("Payment with id 100 not found"));

        mockMvc.perform(get(BASE_URL + "/{id}", paymentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_Success() throws Exception {
        Long paymentId = 1L;
        PaymentStatus newStatus = PaymentStatus.SUCCEEDED;

        PaymentResponse updatedResponse = new PaymentResponse(paymentId, 1L, newStatus, PaymentType.CARD);

        when(paymentService.updateStatus(paymentId, newStatus)).thenReturn(updatedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/status", paymentId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatusWhenPaymentNotFound() throws Exception {
        Long paymentId = 100L;
        PaymentStatus newStatus = PaymentStatus.SUCCEEDED;

        when(paymentService.updateStatus(paymentId, newStatus))
                .thenThrow(new EntityNotFoundException("Payment with id 100 not found"));

        mockMvc.perform(patch(BASE_URL + "/{id}/status", paymentId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatusWithoutParam() throws Exception {
        Long paymentId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/status", paymentId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatusWithInvalidParam() throws Exception {
        Long paymentId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/status", paymentId)
                        .param("status", "invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentsByOrderId_Success() throws Exception {
        Long orderId = 1L;
        List<PaymentResponse> payments = List.of(
                new PaymentResponse(1L, orderId, PaymentStatus.SUCCEEDED, PaymentType.CARD),
                new PaymentResponse(2L, orderId, PaymentStatus.FAILED, PaymentType.CASH)
        );

        when(paymentService.getPaymentsByOrderId(orderId)).thenReturn(payments);

        mockMvc.perform(get(BASE_URL)
                        .param("orderId", orderId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPaymentsByOrderIdWithoutParam() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getPaymentsByOrderIdWhenPaymentsNotFound() throws Exception {
        Long orderId = 100L;
        when(paymentService.getPaymentsByOrderId(orderId)).thenReturn(List.of());

        mockMvc.perform(get(BASE_URL)
                        .param("orderId", orderId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}