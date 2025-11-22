package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.ReviewStatus;

import java.math.BigDecimal;

public record ReviewResponse(Long id,
                             Long userId,
                             Long restaurantId,
                             Long orderId,
                             BigDecimal rating,
                             String comment,
                             ReviewStatus status
) {}
