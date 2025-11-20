package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private AddressDto address;
    private UserRole role;
    private boolean active;
}
