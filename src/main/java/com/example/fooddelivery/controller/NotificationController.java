package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.response.NotificationResponse;
import com.example.fooddelivery.enums.NotificationStatus;
import com.example.fooddelivery.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(@Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.createNotification(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(@RequestParam Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<NotificationResponse> updateNotificationStatus(@PathVariable Long id,
                                                                         @RequestParam NotificationStatus status) {
        return ResponseEntity.ok(notificationService.updateNotificationStatus(id, status));
    }
}
