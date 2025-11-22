package com.example.fooddelivery.dto.response;

import com.example.fooddelivery.enums.NotificationChannel;
import com.example.fooddelivery.enums.NotificationStatus;
import com.example.fooddelivery.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(Long id,
                                   Long userId,
                                   String message,
                                   NotificationType type,
                                   NotificationStatus status,
                                   NotificationChannel channel,
                                   LocalDateTime sendAt,
                                   LocalDateTime sentAt
) {}
