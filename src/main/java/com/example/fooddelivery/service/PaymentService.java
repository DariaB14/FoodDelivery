package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.PaymentMapper;
import com.example.fooddelivery.dto.request.PaymentRequest;
import com.example.fooddelivery.dto.response.PaymentResponse;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.entity.Payment;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.enums.PaymentStatus;
import com.example.fooddelivery.exception.exceptions.EntityNotFoundException;
import com.example.fooddelivery.exception.exceptions.PaymentException;
import com.example.fooddelivery.repository.OrderRepository;
import com.example.fooddelivery.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;

    public PaymentResponse createPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", request.getOrderId())));

        if (paymentRepository.existsByOrderId(request.getOrderId())) {
            throw new PaymentException(String.format("Order with is %d already is payed", request.getOrderId()));
        }

        Payment payment = paymentMapper.toEntity(request);
        payment.setOrder(order);
        Payment savedPayment = paymentRepository.save(payment);
        return paymentMapper.toDto(savedPayment);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Payment with id %d not found", id)));
        return paymentMapper.toDto(payment);
    }

    public PaymentResponse updateStatus(Long id, PaymentStatus status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Payment with id %d not found", id)));

        payment.setPaymentStatus(status);

        Order order = payment.getOrder();

        if (status == PaymentStatus.FAILED) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        }

        if (status == PaymentStatus.SUCCEEDED) {
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
        }
        payment = paymentRepository.save(payment);
        return paymentMapper.toDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(Long orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        return payments.stream()
                .map(paymentMapper::toDto)
                .collect(Collectors.toList());
    }
}
