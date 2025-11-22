package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.response.NotificationResponse;
import com.example.fooddelivery.enums.NotificationStatus;
import com.example.fooddelivery.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification Service")
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "Создать уведомление")
    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Получить все уведомления пользователя")
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @Operation(summary = "Обновить статус уведомления")
    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationResponse> updateNotificationStatus(@PathVariable Long id,
                                                                         @RequestParam NotificationStatus status) {
        return ResponseEntity.ok(notificationService.updateNotificationStatus(id, status));
    }
}
