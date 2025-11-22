package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestBody @Valid CartItemRequest cartItemRequest){
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.addItem(userId, cartItemRequest));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        cartService.deleteItem(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long id, @RequestParam @Min(1) Integer quantity) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.updateQuantity(userId, id, quantity));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        Long userId = getCurrentUserId();
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    //заглушка для security
    private Long getCurrentUserId() {
        return 1L;
    }
}
