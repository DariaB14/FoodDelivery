package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.CourierMapper;
import com.example.fooddelivery.dto.mapper.OrderMapper;
import com.example.fooddelivery.dto.request.CourierRequest;
import com.example.fooddelivery.dto.response.CourierResponse;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.entity.Courier;
import com.example.fooddelivery.entity.Order;
import com.example.fooddelivery.enums.CourierStatus;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.exception.exceptions.BusinessException;
import com.example.fooddelivery.exception.exceptions.EntityNotFoundException;
import com.example.fooddelivery.repository.CourierRepository;
import com.example.fooddelivery.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CourierService {
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final CourierMapper courierMapper;
    private final OrderMapper orderMapper;

    private static final int MAX_ACTIVE_ORDERS = 3;
    private static final BigDecimal MIN_RATING = new BigDecimal("3.0");

    public CourierResponse registerCourier(CourierRequest request) {
        Courier courier = courierMapper.toEntity(request);
        Courier savedCourier = courierRepository.save(courier);
        return courierMapper.toDto(savedCourier);
    }

    @Transactional(readOnly = true)
    public List<CourierResponse> getAllCouriers() {
        return courierRepository.findAll().stream()
                .map(courierMapper::toDto)
                .collect(Collectors.toList());
    }

    public CourierResponse assignOrder(Long courierId, Long orderId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Courier with id %d not found", courierId)));

        if (courier.getStatus() == CourierStatus.OFFLINE) {
            throw new BusinessException("Courier must be ONLINE to accept orders");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", orderId)));

        if (order.getCourier() != null) {
            throw new BusinessException("Order is already assigned");
        }

        if (order.getStatus() != OrderStatus.READY) {
            throw new BusinessException("Order must be in READY status for assignment");
        }

        if (courier.getCurrentOrdersAmount() >= MAX_ACTIVE_ORDERS) {
            throw new BusinessException("Courier cannot have more than " + MAX_ACTIVE_ORDERS + " active orders");
        }

        if (courier.getRating().compareTo(MIN_RATING) < 0) {
            throw new BusinessException("Courier with rating below 3 cannot be auto-assigned");
        }

        order.setCourier(courier);
        order.setStatus(OrderStatus.TAKED);

        courier.setCurrentOrdersAmount(courier.getCurrentOrdersAmount() + 1);

        orderRepository.save(order);
        Courier updatedCourier = courierRepository.save(courier);

        return courierMapper.toDto(updatedCourier);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveOrders(Long courierId) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Courier with id %d not found", courierId)));

        return courier.getOrders().stream()
                .filter(order -> order.getStatus() == OrderStatus.TAKED)
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public CourierResponse updateStatus(Long courierId, CourierStatus status) {
        Courier courier = courierRepository.findById(courierId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Courier with id %d not found", courierId)));

        // Дополнительные проверки при смене статуса
        if (status == CourierStatus.OFFLINE && courier.getCurrentOrdersAmount() > 0) {
            throw new BusinessException("Cannot go offline with active orders");
        }

        courier.setStatus(status);
        Courier updatedCourier = courierRepository.save(courier);
        return courierMapper.toDto(updatedCourier);
    }
}
