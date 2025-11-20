package com.example.fooddelivery.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record ItemResponse(
        Long id,
        String name,
        BigDecimal minPrice,
        Integer minPreparationMinutes,
        boolean available,
        Long restaurantId,
        List<ItemOptionResponse> options
) {}
