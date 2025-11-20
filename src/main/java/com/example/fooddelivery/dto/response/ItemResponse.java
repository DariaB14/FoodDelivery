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
) {
    public ItemResponse {
        minPrice = calculateMinPrice(options);
        minPreparationMinutes = calculatePreparationMinutes(options);
    }

    private static BigDecimal calculateMinPrice(List<ItemOptionResponse> options){
        if(options == null || options.isEmpty()){
            return BigDecimal.ZERO;
        }

        return options.stream()
                .map(ItemOptionResponse::price)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private static Integer calculatePreparationMinutes(List<ItemOptionResponse> options){
        if(options == null || options.isEmpty()){
            return 0;
        }

        return options.stream()
                .map(ItemOptionResponse::preparationMinutes)
                .min(Integer::compareTo)
                .orElse(0);
    }
}
