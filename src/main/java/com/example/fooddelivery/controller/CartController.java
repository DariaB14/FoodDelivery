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
        return ResponseEntity.ok(cartService.addItem(cartItemRequest));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        cartService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long id, @RequestParam @Min(1) Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(id, quantity));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build();
    }
}
