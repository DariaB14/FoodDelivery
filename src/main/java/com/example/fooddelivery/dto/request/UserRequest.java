package com.example.fooddelivery.dto.request;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.enums.UserRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest{
    @NotBlank String username;
    @Email String email;
    @Size(min = 6) String password;
    @Pattern(regexp = "^\\+7\\d{10}$") String phone;
    @Valid AddressDto address;
    @NotNull UserRole role;
}



