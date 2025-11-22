package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CourierRequest;
import com.example.fooddelivery.dto.response.CourierResponse;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.enums.CourierStatus;
import com.example.fooddelivery.service.CourierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/couriers")
@RequiredArgsConstructor
public class CourierController {
    private final CourierService courierService;

    @PostMapping
    public ResponseEntity<CourierResponse> registerCourier(@Valid @RequestBody CourierRequest request) {
        CourierResponse response = courierService.registerCourier(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<CourierResponse>> getAllCouriers() {
        return ResponseEntity.ok(courierService.getAllCouriers());
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<CourierResponse> assignOrder(@PathVariable Long id,
                                                       @RequestParam Long orderId) {
        return ResponseEntity.ok(courierService.assignOrder(id, orderId));
    }

    @GetMapping("/{id}/orders")
    public ResponseEntity<List<OrderResponse>> getActiveOrders(@PathVariable Long id) {
        return ResponseEntity.ok(courierService.getActiveOrders(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CourierResponse> updateStatus(@PathVariable Long id,
                                                        @RequestParam CourierStatus status) {
        return ResponseEntity.ok(courierService.updateStatus(id, status));
    }
}
