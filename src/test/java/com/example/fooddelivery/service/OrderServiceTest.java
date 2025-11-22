package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.OrderMapper;
import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.entity.*;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.exception.AccessDeniedException;
import com.example.fooddelivery.exception.BusinessException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.exception.StatusException;
import com.example.fooddelivery.repository.CartRepository;
import com.example.fooddelivery.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    private final Long ORDER_ID = 1L;
    private final Long USER_ID = 1L;
    private final Long CART_ID = 1L;
    private final Long COURIER_ID = 1L;
    private final Long NON_EXISTENT_ORDER_ID = 999L;

    private OrderRequest orderRequest;
    private OrderResponse orderResponse;
    private Order order;
    private Cart cart;
    private User user;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest(CART_ID);
        orderResponse = new OrderResponse(ORDER_ID, USER_ID, CART_ID, OrderStatus.NEW, new BigDecimal("500.00"));

        user = new User();
        user.setId(USER_ID);

        cart = new Cart();
        cart.setId(CART_ID);
        cart.setUser(user);

        order = new Order();
        order.setId(ORDER_ID);
        order.setUser(user);
        order.setCart(cart);
        order.setStatus(OrderStatus.NEW);
        order.setTotalAmount(new BigDecimal("500.00"));
    }

    @Test
    void createOrder_Success() {
        CartItem cartItem = new CartItem();
        Item item = new Item();
        item.setAvailable(true);
        item.setName("Pizza");
        ItemOption itemOption = new ItemOption();
        itemOption.setPrice(new BigDecimal("500.00"));
        cartItem.setItem(item);
        cartItem.setItemOption(itemOption);
        cartItem.setQuantity(1);

        cart.setItems(new ArrayList<>(List.of(cartItem)));

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.createOrder(orderRequest);

        assertThat(result).isNotNull();

        verify(cartRepository).save(cart);
        verify(orderRepository).save(order);
        verify(notificationService).createNotification(any(NotificationRequest.class));
    }

    @Test
    void createOrderWhenCartNotFound() {
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Cart with id 1 not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderWhenCartIsEmpty() {
        cart.setItems(List.of());
        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order cannot be created without items");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderWhenItemNotAvailable() {
        CartItem cartItem = new CartItem();
        Item item = new Item();
        item.setAvailable(false);
        item.setName("Pizza");
        cartItem.setItem(item);
        cart.setItems(List.of(cartItem));

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("These items are not available now: Pizza");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void createOrderWhenAmountLessThanMinimum() {
        CartItem cartItem = new CartItem();
        Item item = new Item();
        item.setAvailable(true);
        ItemOption itemOption = new ItemOption();
        itemOption.setPrice(new BigDecimal("100.00"));
        cartItem.setItem(item);
        cartItem.setItemOption(itemOption);
        cartItem.setQuantity(1);
        cart.setItems(List.of(cartItem));

        when(cartRepository.findById(CART_ID)).thenReturn(Optional.of(cart));

        assertThatThrownBy(() -> orderService.createOrder(orderRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order cannot be created with total amount smaller than 300.00");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrderById_Success() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.getOrderById(ORDER_ID);

        assertThat(result).isNotNull();

        verify(orderRepository).findById(ORDER_ID);
    }

    @Test
    void getOrderByIdWhenOrderNotFound() {
        when(orderRepository.findById(NON_EXISTENT_ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(NON_EXISTENT_ORDER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order with id 999 not found");
    }

    @Test
    void getOrdersByUserId_Success() {
        when(orderRepository.findByUserId(USER_ID)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersById(USER_ID);

        assertThat(result).hasSize(1);

        verify(orderRepository).findByUserId(USER_ID);
    }

    @Test
    void getOrdersByStatus_Success() {
        when(orderRepository.findByStatus(OrderStatus.NEW)).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getOrdersByStatus(OrderStatus.NEW);

        assertThat(result).hasSize(1);

        verify(orderRepository).findByStatus(OrderStatus.NEW);
    }

    @Test
    void getAllOrders_Success() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.getAllOrders();

        assertThat(result).hasSize(1);

        verify(orderRepository).findAll();
    }

    @Test
    void updateStatus_Success() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.updateStatus(ORDER_ID, OrderStatus.CONFIRMED);

        assertThat(result).isNotNull();

        verify(orderRepository).save(order);
        verify(notificationService).createNotification(any(NotificationRequest.class));
    }

    @Test
    void updateStatusToDelivered_Success() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.updateStatus(ORDER_ID, OrderStatus.DELIVERED);

        assertThat(result).isNotNull();

        verify(notificationService, times(2)).createNotification(any(NotificationRequest.class));
    }

    @Test
    void updateStatusWhenOrderNotFound() {
        when(orderRepository.findById(NON_EXISTENT_ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateStatus(NON_EXISTENT_ORDER_ID, OrderStatus.CONFIRMED))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order with id 999 not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatusWhenOrderDelivered() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, OrderStatus.CONFIRMED))
                .isInstanceOf(StatusException.class)
                .hasMessage("Delivered order`s status cannot be updated");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatusByCourier_Success() {
        Courier courier = new Courier();
        courier.setId(COURIER_ID);
        order.setCourier(courier);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.updateStatusByCourier(ORDER_ID, OrderStatus.TAKED, COURIER_ID);

        assertThat(result).isNotNull();

        verify(orderRepository).save(order);
    }

    @Test
    void updateStatusByCourierWhenCourierNotAssigned() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatusByCourier(ORDER_ID, OrderStatus.TAKED, COURIER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("Order is not assigned to any courier");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatusByCourierWhenWrongCourier() {
        Courier courier = new Courier();
        courier.setId(2L);
        order.setCourier(courier);

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.updateStatusByCourier(ORDER_ID, OrderStatus.TAKED, COURIER_ID))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("This order belongs to another courier");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_Success() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);

        orderService.cancelOrder(ORDER_ID);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(orderRepository).save(order);
        verify(notificationService).createNotification(any(NotificationRequest.class));
    }

    @Test
    void cancelOrderWhenOrderNotFound() {
        when(orderRepository.findById(NON_EXISTENT_ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancelOrder(NON_EXISTENT_ORDER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order with id 999 not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrderWhenOrderDelivered() {
        order.setStatus(OrderStatus.DELIVERED);
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.cancelOrder(ORDER_ID))
                .isInstanceOf(StatusException.class)
                .hasMessage("Delivered order cannot be cancelled");

        verify(orderRepository, never()).save(any());
    }

}