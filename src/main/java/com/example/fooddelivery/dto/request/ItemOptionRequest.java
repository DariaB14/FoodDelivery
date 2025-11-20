package com.example.fooddelivery.dto.request;

import com.example.fooddelivery.enums.ItemSize;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemOptionRequest {
    @NotNull ItemSize size;
    @NotNull @DecimalMin("0.0") BigDecimal price;
    @NotNull @Min(1) Integer preparationMinutes;
}
