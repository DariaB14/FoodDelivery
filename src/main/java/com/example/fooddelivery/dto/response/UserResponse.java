package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.enums.UserRole;

public record UserResponse(
        Long id,
        String username,
        String email,
        String phone,
        AddressDto address,
        UserRole role,
        boolean active
) {}

