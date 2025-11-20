package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.ItemSize;

import java.math.BigDecimal;

public record CartItemResponse (Long id,
                                Long itemId,
                                Long itemOptionId,
                                Integer quantity
) {}
