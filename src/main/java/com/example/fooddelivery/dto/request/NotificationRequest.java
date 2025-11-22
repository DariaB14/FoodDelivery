package com.example.fooddelivery.dto.request;

import com.example.fooddelivery.enums.NotificationChannel;
import com.example.fooddelivery.enums.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    @NotNull Long userId;
    @NotBlank String message;
    @NotNull NotificationType type;
    @NotNull NotificationChannel channel;
    LocalDateTime sendAt;
}
