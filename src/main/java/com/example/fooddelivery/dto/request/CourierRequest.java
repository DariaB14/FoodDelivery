package com.example.fooddelivery.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CourierRequest {
    @NotBlank String name;
    @Pattern(regexp = "^\\+7\\d{10}$", message = "Phone format: +7xxxxxxxxxx") String phone;
}
