package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.enums.CuisineType;

import java.time.LocalTime;

public record RestaurantResponse(
        Long id,
        String name,
        AddressDto address,
        CuisineType cuisineType,
        Double rating,
        LocalTime openingTime,
        LocalTime closingTime,
        Boolean isOpen
) {}
