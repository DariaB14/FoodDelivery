package com.example.fooddelivery.dto.response;

import java.util.List;

public record CartResponse(Long id,
                           Long userId,
                           Long restaurantId,
                           List<CartItemResponse> items,
                           Integer eta
) {}
