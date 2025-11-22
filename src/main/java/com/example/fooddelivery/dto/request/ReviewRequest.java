package com.example.fooddelivery.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {
    @NotNull Long orderId;
    @NotNull @DecimalMin("1.0") @DecimalMax("5.0") BigDecimal rating;
    @NotBlank @Size(max = 1000) String comment;
}
