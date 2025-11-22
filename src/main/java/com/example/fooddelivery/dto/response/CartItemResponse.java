package com.example.fooddelivery.dto.response;

public record CartItemResponse (Long id,
                                Long itemId,
                                Long itemOptionId,
                                Integer quantity
) {}
