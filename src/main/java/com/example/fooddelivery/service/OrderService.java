package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.OrderMapper;
import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.entity.*;
import com.example.fooddelivery.enums.NotificationChannel;
import com.example.fooddelivery.enums.NotificationType;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.exception.AccessDeniedException;
import com.example.fooddelivery.exception.BusinessException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.exception.StatusException;
import com.example.fooddelivery.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {
    private final NotificationService notificationService;
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("300.00");

    public OrderResponse createOrder(OrderRequest request){
        Cart cart = cartRepository.findById(request.getCartId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Cart with id %d not found", request.getCartId())));

        if (cart.getItems().isEmpty()){
            throw new BusinessException("Order cannot be created without items");
        }

        checkItemAvailability(cart);

        BigDecimal totalAmount = calculateAmountFromCart(cart);

        if (totalAmount.compareTo(MIN_AMOUNT) < 0){
            throw new BusinessException("Order cannot be created with total amount smaller than 300.00");
        }

        Order order = orderMapper.toEntity(request);
        order.setUser(cart.getUser());
        order.setStatus(OrderStatus.NEW);
        order.setCart(cart);
        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        sendNotification(order);

        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", id)));
        return orderMapper.toDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersById(Long userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    public OrderResponse updateStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", orderId)));

        validateStatusUpdate(order, status, null);
        order.setStatus(status);
        orderRepository.save(order);

        sendNotification(order);
        if (status == OrderStatus.DELIVERED) {
            scheduleNotification(order);
        }

        return orderMapper.toDto(order);
    }

    public OrderResponse updateStatusByCourier(Long orderId, OrderStatus status, Long courierId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", orderId)));

        validateStatusUpdate(order, status, courierId);

        order.setStatus(status);
        orderRepository.save(order);

        sendNotification(order);

        if(status == OrderStatus.DELIVERED){
            scheduleNotification(order);
        }

        return orderMapper.toDto(order);
    }

    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Order with id %d not found", orderId)));
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new StatusException("Delivered order cannot be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        sendNotification(order);
    }

    private void checkItemAvailability(Cart cart) {
        List<String> unavailableItems = new ArrayList<>();
        for (CartItem cartItem : cart.getItems()){
            Item item = cartItem.getItem();
            if(!item.isAvailable()){
                unavailableItems.add(item.getName());
            }
        }

        if (!unavailableItems.isEmpty()) {
            throw new BusinessException("These items are not available now: " + String.join(", ", unavailableItems));
        }
    }

    private void validateStatusUpdate(Order order, OrderStatus status, Long courierId){
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new StatusException("Delivered order`s status cannot be updated");
        }

        if (courierId != null) {
            if (order.getCourier() == null) {
                throw new AccessDeniedException("Order is not assigned to any courier");
            }
            if (!order.getCourier().getId().equals(courierId)) {
                throw new AccessDeniedException("This order belongs to another courier");
            }
        }
    }

    private BigDecimal calculateAmountFromCart(Cart cart) {
        return cart.getItems().stream()
                .map(this::calculateItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateItemTotal(CartItem cartItem) {
        BigDecimal price = cartItem.getItemOption().getPrice();
        int quantity = cartItem.getQuantity();
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    private void sendNotification(Order order){
        OrderStatus orderStatus = order.getStatus();
        String message = generateMessage(orderStatus);
        NotificationType type = generateNotificationType(orderStatus);
        NotificationChannel channel = NotificationChannel.PUSH;

        NotificationRequest request = new NotificationRequest(order.getUser().getId(), message, type, channel, null);

        notificationService.createNotification(request);
    }

    private String generateMessage(OrderStatus status){
        switch (status){
            case NEW -> {
                return "Заказ ожидает оплаты";
            }
            case CONFIRMED -> {
                return "Заказ подтвержден";
            }
            case CANCELLED -> {
                return "Платеж не прошел, заказ отменен";
            }
            case READY -> {
                return "Заказ готов к выдаче";
            }
            case TAKED -> {
                return "Курьер в пути";
            }
            case DELIVERED -> {
                return "Заказ доставлен";
            }
            default -> {
                return "";
            }
        }
    }

    private NotificationType generateNotificationType(OrderStatus status){
        switch (status){
            case NEW -> {
                return NotificationType.ORDER_CREATED;
            }
            case CANCELLED -> {
                return NotificationType.PAYMENT_FAILED;
            }
            case CONFIRMED -> {
                return NotificationType.PAYMENT_SUCCEEDED;
            }
            case READY -> {
                return NotificationType.ORDER_READY;
            }
            case TAKED -> {
                return NotificationType.ORDER_DELIVERING;
            }
            case DELIVERED -> {
                return NotificationType.ORDER_DELIVERED;
            }
            default -> {
                return NotificationType.PROMOTIONAL;
            }
        }
    }

    private void scheduleNotification(Order order){
        NotificationRequest request = new NotificationRequest(
                order.getUser().getId(),
                "Не забудьте оценить ваш заказ",
                NotificationType.REVIEW_REMINDER,
                NotificationChannel.PUSH,
                LocalDateTime.now().plusMinutes(30)
        );
        notificationService.createNotification(request);
    }
}
