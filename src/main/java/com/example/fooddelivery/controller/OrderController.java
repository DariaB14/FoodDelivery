package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.enums.OrderStatus;
import com.example.fooddelivery.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order Service")
public class OrderController {
    private final OrderService orderService;

    @Operation(summary = "Создать заказ на основе корзины")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse response = orderService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Найти заказ")
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Найти заказы по id пользователя и статусу")
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getOrders(@RequestParam(required = false) Long userId,
                                                         @RequestParam(required = false) OrderStatus status){
        List<OrderResponse> response = new ArrayList<>();
        if(userId != null){
            response = orderService.getOrdersById(userId);
        } else if(status != null){
            response = orderService.getOrdersByStatus(status);
        } else {
            response = orderService.getAllOrders();
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Обновить статус заказа")
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @RequestParam @NotNull OrderStatus status,
                                                      @RequestParam(required = false) Long courierId){
        if(courierId!=null){
            return ResponseEntity.ok(orderService.updateStatusByCourier(id, status, courierId));
        } else {
            return ResponseEntity.ok(orderService.updateStatus(id, status));
        }
    }

    @Operation(summary = "Отменить заказ")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id){
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }
}
