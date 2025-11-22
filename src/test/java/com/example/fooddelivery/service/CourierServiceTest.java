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
import com.example.fooddelivery.exception.BusinessException;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.repository.CourierRepository;
import com.example.fooddelivery.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourierServiceTest {
    @Mock
    private CourierRepository courierRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CourierMapper courierMapper;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private CourierService courierService;

    private final Long COURIER_ID = 1L;
    private final Long ORDER_ID = 1L;
    private final Long NON_EXISTENT_COURIER_ID = 999L;
    private final Long NON_EXISTENT_ORDER_ID = 999L;

    private CourierRequest courierRequest;
    private CourierResponse courierResponse;
    private Courier courier;
    private Order order;

    @BeforeEach
    void setUp() {
        courierRequest = new CourierRequest("Max", "+79991234567");
        courierResponse = new CourierResponse(1L, "Max", "+79991234567", CourierStatus.FREE, new BigDecimal("4.5"), 1);

        courier = new Courier();
        courier.setId(COURIER_ID);
        courier.setName("Max");
        courier.setPhone("+79991234567");
        courier.setStatus(CourierStatus.FREE);
        courier.setRating(new BigDecimal("4.5"));
        courier.setCurrentOrdersAmount(1);

        order = new Order();
        order.setId(ORDER_ID);
        order.setStatus(OrderStatus.READY);
    }

    @Test
    void registerCourier_Success() {
        Courier newCourier = new Courier();
        newCourier.setName("Max");
        newCourier.setPhone("+79991234567");

        when(courierMapper.toEntity(courierRequest)).thenReturn(newCourier);
        when(courierRepository.save(newCourier)).thenReturn(courier);
        when(courierMapper.toDto(courier)).thenReturn(courierResponse);

        CourierResponse result = courierService.registerCourier(courierRequest);

        assertThat(result).isNotNull();

        verify(courierRepository).save(newCourier);
    }

    @Test
    void getAllCouriers_Success() {
        when(courierRepository.findAll()).thenReturn(List.of(courier));
        when(courierMapper.toDto(courier)).thenReturn(courierResponse);

        List<CourierResponse> result = courierService.getAllCouriers();

        assertThat(result).hasSize(1);

        verify(courierRepository).findAll();
    }

    @Test
    void assignOrder_Success() {
        courier.setStatus(CourierStatus.FREE);
        courier.setCurrentOrdersAmount(0);
        courier.setRating(new BigDecimal("4.5"));

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(courierRepository.save(courier)).thenReturn(courier);
        when(courierMapper.toDto(courier)).thenReturn(courierResponse);

        CourierResponse result = courierService.assignOrder(COURIER_ID, ORDER_ID);

        assertThat(result).isNotNull();

        verify(orderRepository).save(order);
        verify(courierRepository).save(courier);
    }

    @Test
    void assignOrderWhenCourierNotFound() {
        when(courierRepository.findById(NON_EXISTENT_COURIER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.assignOrder(NON_EXISTENT_COURIER_ID, ORDER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Courier with id 999 not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignOrderWhenOrderNotFound() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(orderRepository.findById(NON_EXISTENT_ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.assignOrder(COURIER_ID, NON_EXISTENT_ORDER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Order with id 999 not found");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignOrderWhenOrderAlreadyAssigned() {
        Courier anotherCourier = new Courier();
        anotherCourier.setId(2L);
        order.setCourier(anotherCourier);

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> courierService.assignOrder(COURIER_ID, ORDER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order is already assigned");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignOrderWhenOrderNotReady() {
        order.setStatus(OrderStatus.CONFIRMED);

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> courierService.assignOrder(COURIER_ID, ORDER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Order must be in READY status for assignment");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignOrderWhenCourierHasMaxOrders() {
        courier.setCurrentOrdersAmount(3);

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> courierService.assignOrder(COURIER_ID, ORDER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Courier cannot have more than 3 active orders");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void assignOrderWhenCourierRatingLow() {
        courier.setRating(new BigDecimal("2.5"));

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> courierService.assignOrder(COURIER_ID, ORDER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Courier with rating below 3 cannot be auto-assigned");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void getActiveOrders_Success() {
        Order activeOrder = new Order();
        activeOrder.setId(ORDER_ID);
        activeOrder.setStatus(OrderStatus.TAKED);
        activeOrder.setCourier(courier);

        Order deliveredOrder = new Order();
        deliveredOrder.setId(2L);
        deliveredOrder.setStatus(OrderStatus.DELIVERED);
        deliveredOrder.setCourier(courier);

        courier.setOrders(List.of(activeOrder, deliveredOrder));

        OrderResponse orderResponse = new OrderResponse(ORDER_ID, 1L, 1L, OrderStatus.TAKED, new BigDecimal("500.00"));

        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(orderMapper.toDto(activeOrder)).thenReturn(orderResponse);

        List<OrderResponse> result = courierService.getActiveOrders(COURIER_ID);

        assertThat(result).hasSize(1);

        verify(courierRepository).findById(COURIER_ID);
    }

    @Test
    void getActiveOrdersWhenCourierNotFound() {
        when(courierRepository.findById(NON_EXISTENT_COURIER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.getActiveOrders(NON_EXISTENT_COURIER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Courier with id 999 not found");
    }

    @Test
    void updateStatus_Success() {
        when(courierRepository.findById(COURIER_ID)).thenReturn(Optional.of(courier));
        when(courierRepository.save(courier)).thenReturn(courier);
        when(courierMapper.toDto(courier)).thenReturn(courierResponse);

        CourierResponse result = courierService.updateStatus(COURIER_ID, CourierStatus.BUSY);

        assertThat(result).isNotNull();

        verify(courierRepository).save(courier);
    }

    @Test
    void updateStatusWhenCourierNotFound() {
        when(courierRepository.findById(NON_EXISTENT_COURIER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courierService.updateStatus(NON_EXISTENT_COURIER_ID, CourierStatus.BUSY))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Courier with id 999 not found");

        verify(courierRepository, never()).save(any());
    }

}