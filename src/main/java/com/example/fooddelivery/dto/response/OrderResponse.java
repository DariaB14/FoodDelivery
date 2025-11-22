package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.OrderStatus;

import java.math.BigDecimal;

public record OrderResponse(Long id,
                            Long userId,
                            Long cartId,
                            OrderStatus status,
                            BigDecimal totalAmount) {
}
