package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.NotificationMapper;
import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.response.NotificationResponse;
import com.example.fooddelivery.entity.Notification;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.NotificationStatus;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.inner.NotificationSender;
import com.example.fooddelivery.repository.NotificationRepository;
import com.example.fooddelivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;
    private final NotificationSender notificationSender;

    public NotificationResponse createNotification(NotificationRequest request){
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("User with id %d not found", request.getUserId())));

        Notification notification = notificationMapper.toEntity(request);
        notification.setUser(user);
        notification.setSendAt(request.getSendAt());

        if (request.getSendAt() != null && request.getSendAt().isAfter(LocalDateTime.now())) {
            notification.setStatus(NotificationStatus.SCHEDULED);
        } else {
            sendNotification(notification);
        }

        notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        if(!userRepository.existsById(userId)){
            throw new EntityNotFoundException(String.format("User with id %d not found", userId));
        }

        List<Notification> notifications = notificationRepository.findAllByUserId(userId);
        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }

    public NotificationResponse updateNotificationStatus(Long id, NotificationStatus status) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Notification with id %d not found", id)));

        notification.setStatus(status);

        if (status == NotificationStatus.SENT) {
            notification.setSentAt(LocalDateTime.now());
        }

        notificationRepository.save(notification);
        return notificationMapper.toDto(notification);
    }

    private void sendNotification(Notification notification) {
        try {
            notificationSender.send(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void processScheduledNotifications() {
        List<Notification> scheduled = notificationRepository.findAllByStatusAndSendAtLessThanEqual(NotificationStatus.SCHEDULED, LocalDateTime.now());

        for (Notification notification : scheduled) {
            try {
                sendNotification(notification);
            } catch (Exception e) {
                notification.setStatus(NotificationStatus.FAILED);
            }
            notificationRepository.save(notification);
        }
    }
}
