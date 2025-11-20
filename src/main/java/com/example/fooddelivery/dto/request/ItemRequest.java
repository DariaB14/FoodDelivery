package com.example.fooddelivery.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    @NotBlank String name;
    @NotNull @DecimalMin("0.0") BigDecimal minPrice;
    @NotNull @Min(1) Integer minPreparationMinutes;

    boolean available = true;

    List<ItemOptionRequest> options = new ArrayList<>();
}
