package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.ItemSize;

import java.math.BigDecimal;

public record ItemOptionResponse (
        Long id,
        ItemSize size,
        BigDecimal price,
        Integer preparationMinutes
) {}
