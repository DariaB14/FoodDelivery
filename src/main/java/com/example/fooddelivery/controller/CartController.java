package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart Service")
public class CartController {
    private final CartService cartService;

    @Operation(summary = "Добавить блюдо в корзину")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestBody @Valid CartItemRequest cartItemRequest){
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.addItem(userId, cartItemRequest));
    }

    @Operation(summary = "Удалить блюдо из корзины")
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        cartService.deleteItem(userId, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Изменить количество")
    @PutMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long id, @RequestParam @Min(1) Integer quantity) {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.updateQuantity(userId, id, quantity));
    }

    @Operation(summary = "Просмотреть корзину")
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        Long userId = getCurrentUserId();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @Operation(summary = "Очистить корзину")
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
