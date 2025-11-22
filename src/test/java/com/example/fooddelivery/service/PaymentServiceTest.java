package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.PaymentMapper;
import com.example.fooddelivery.dto.request.PaymentRequest;
import com.example.fooddelivery.dto.response.PaymentResponse;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.Payment;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.enums.PaymentStatus;
import com.example.fooddelivery.enums.PaymentType;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.exception.PaymentException;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentService paymentService;

    private final Long PAYMENT_ID = 1L;
    private final Long ORDER_ID = 1L;
    private final Long NON_EXISTENT_PAYMENT_ID = 999L;
    private final Long NON_EXISTENT_ORDER_ID = 999L;

    private PaymentRequest paymentRequest;
    private PaymentResponse paymentResponse;
    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
        paymentRequest = new PaymentRequest(ORDER_ID, PaymentType.CARD);
        paymentResponse = new PaymentResponse(PAYMENT_ID, ORDER_ID, PaymentStatus.PENDING, PaymentType.CARD);

        order = new Order();
        order.setId(ORDER_ID);
        order.setStatus(OrderStatus.NEW);

        payment = new Payment();
        payment.setId(PAYMENT_ID);
        payment.setOrder(order);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPaymentType(PaymentType.CARD);
    }

    @Test
    void createPayment_Success() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(paymentMapper.toEntity(paymentRequest)).thenReturn(payment);
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.createPayment(paymentRequest);

        assertThat(result).isNotNull();

        verify(paymentRepository).save(payment);
    }

    @Test
    void createPaymentWhenOrderNotFound() {
        when(orderRepository.findById(NON_EXISTENT_ORDER_ID)).thenReturn(Optional.empty());
        PaymentRequest request = new PaymentRequest(NON_EXISTENT_ORDER_ID, PaymentType.CARD);

        assertThatThrownBy(() -> paymentService.createPayment(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order with id 999 not found");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void createPaymentWhenOrderAlreadyPaid() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(paymentRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

        assertThatThrownBy(() -> paymentService.createPayment(paymentRequest))
                .isInstanceOf(PaymentException.class)
                .hasMessage("Order with is 1 already is payed");

        verify(paymentRepository, never()).save(any());
    }

    @Test
    void getPayment_Success() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.getPayment(PAYMENT_ID);

        assertThat(result).isNotNull();

        verify(paymentRepository).findById(PAYMENT_ID);
    }

    @Test
    void getPaymentWhenPaymentNotFound() {
        when(paymentRepository.findById(NON_EXISTENT_PAYMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.getPayment(NON_EXISTENT_PAYMENT_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Payment with id 999 not found");
    }

    @Test
    void updateStatusToSucceeded_Success() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.updateStatus(PAYMENT_ID, PaymentStatus.SUCCEEDED);

        assertThat(result).isNotNull();
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SUCCEEDED);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);

        verify(orderRepository).save(order);
        verify(paymentRepository).save(payment);
    }

    @Test
    void updateStatusToFailed_Success() {
        when(paymentRepository.findById(PAYMENT_ID)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);

        PaymentResponse result = paymentService.updateStatus(PAYMENT_ID, PaymentStatus.FAILED);

        assertThat(result).isNotNull();

        verify(orderRepository).save(order);
        verify(paymentRepository).save(payment);
    }

    @Test
    void updateStatusWhenPaymentNotFound() {
        when(paymentRepository.findById(NON_EXISTENT_PAYMENT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.updateStatus(NON_EXISTENT_PAYMENT_ID, PaymentStatus.SUCCEEDED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Payment with id 999 not found");

        verify(paymentRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getPaymentsByOrderId_Success() {
        List<Payment> payments = List.of(payment);
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(payments);
        when(paymentMapper.toDto(payment)).thenReturn(paymentResponse);

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(ORDER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(PAYMENT_ID);

        verify(paymentRepository).findByOrderId(ORDER_ID);
    }

    @Test
    void getPaymentsByOrderId_EmptyList() {
        when(paymentRepository.findByOrderId(ORDER_ID)).thenReturn(List.of());

        List<PaymentResponse> result = paymentService.getPaymentsByOrderId(ORDER_ID);

        assertThat(result).isEmpty();

        verify(paymentRepository).findByOrderId(ORDER_ID);
    }
}