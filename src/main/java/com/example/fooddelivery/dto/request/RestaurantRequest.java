package com.example.fooddelivery.dto.request;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.enums.CuisineType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RestaurantRequest {
    @NotBlank String name;
    @NotNull AddressDto address;
    @NotNull CuisineType cuisineType;
    @NotNull LocalTime openingTime;
    @NotNull LocalTime closingTime;
}
