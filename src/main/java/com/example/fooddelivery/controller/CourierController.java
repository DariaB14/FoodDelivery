package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CourierRequest;
import com.example.fooddelivery.dto.response.CourierResponse;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.enums.CourierStatus;
import com.example.fooddelivery.service.CourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/couriers")
@RequiredArgsConstructor
@Tag(name = "Courier Service")
public class CourierController {
    private final CourierService courierService;

    @Operation(summary = "Регистрация курьера")
    @PostMapping
    public ResponseEntity<CourierResponse> registerCourier(@Valid @RequestBody CourierRequest request) {
        CourierResponse response = courierService.registerCourier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Найти всех курьеров")
    @GetMapping
    public ResponseEntity<List<CourierResponse>> getAllCouriers() {
        return ResponseEntity.ok(courierService.getAllCouriers());
    }

    @Operation(summary = "Назначить курьера на заказ")
    @PatchMapping("/{id}/assign")
    public ResponseEntity<CourierResponse> assignOrder(@PathVariable Long id,
                                                       @RequestParam Long orderId) {
        return ResponseEntity.ok(courierService.assignOrder(id, orderId));
    }

    @Operation(summary = "Найти активные заказы курьера")
    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(@PathVariable Long id) {
        return ResponseEntity.ok(courierService.getActiveOrders(id));
    }

    @Operation(summary = "Изменить статус курьера")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CourierResponse> updateStatus(@PathVariable Long id,
                                                        @RequestParam CourierStatus status) {
        return ResponseEntity.ok(courierService.updateStatus(id, status));
    }
}
