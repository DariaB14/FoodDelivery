package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.mapper.NotificationMapper;
import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.response.NotificationResponse;
import com.example.fooddelivery.entity.Notification;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.enums.NotificationChannel;
import com.example.fooddelivery.enums.NotificationStatus;
import com.example.fooddelivery.enums.NotificationType;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.inner.NotificationSender;
import com.example.fooddelivery.repository.NotificationRepository;
import com.example.fooddelivery.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationSender notificationSender;

    @InjectMocks
    private NotificationService notificationService;

    private final Long NOTIFICATION_ID = 1L;
    private final Long USER_ID = 1L;
    private final Long NON_EXISTENT_USER_ID = 999L;
    private final Long NON_EXISTENT_NOTIFICATION_ID = 999L;

    private NotificationRequest notificationRequest;
    private NotificationResponse notificationResponse;
    private Notification notification;
    private User user;

    @BeforeEach
    void setUp() {
        notificationRequest = new NotificationRequest(USER_ID, "Заказ готов к выдаче", NotificationType.ORDER_READY, NotificationChannel.PUSH, LocalDateTime.now());
        notificationResponse = new NotificationResponse(NOTIFICATION_ID, USER_ID, "Заказ готов к выдаче", NotificationType.ORDER_READY,
                NotificationStatus.PENDING, NotificationChannel.PUSH, LocalDateTime.now(), null);

        user = new User();
        user.setId(USER_ID);

        notification = new Notification();
        notification.setId(NOTIFICATION_ID);
        notification.setUser(user);
        notification.setMessage("Заказ готов к выдаче");
        notification.setType(NotificationType.ORDER_READY);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setChannel(NotificationChannel.PUSH);
        notification.setSendAt(LocalDateTime.now());
    }

    @Test
    void createNotification_Success() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(notificationMapper.toEntity(notificationRequest)).thenReturn(notification);
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationMapper.toDto(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.createNotification(notificationRequest);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(NOTIFICATION_ID);
        assertThat(result.userId()).isEqualTo(USER_ID);

        verify(notificationRepository).save(notification);
    }

    @Test
    void createNotificationWhenUserNotFound() {
        when(userRepository.findById(NON_EXISTENT_USER_ID)).thenReturn(Optional.empty());
        NotificationRequest request = new NotificationRequest(NON_EXISTENT_USER_ID, "Заказ готов к выдаче",
                NotificationType.ORDER_READY, NotificationChannel.PUSH, null);

        assertThatThrownBy(() -> notificationService.createNotification(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id 999 not found");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void createNotificationThenSendImmediately() throws Exception {
        NotificationRequest request = new NotificationRequest(USER_ID, "Заказ готов к выдаче",
                NotificationType.ORDER_READY, NotificationChannel.PUSH, null);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(notificationMapper.toEntity(request)).thenReturn(notification);
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationMapper.toDto(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.createNotification(request);

        assertThat(result).isNotNull();

        verify(notificationSender).send(notification);
        verify(notificationRepository).save(notification);
    }

    @Test
    void getUserNotifications_Success() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(notificationRepository.findAllByUserId(USER_ID)).thenReturn(List.of(notification));
        when(notificationMapper.toDto(notification)).thenReturn(notificationResponse);

        List<NotificationResponse> result = notificationService.getUserNotifications(USER_ID);

        assertThat(result).hasSize(1);

        verify(notificationRepository).findAllByUserId(USER_ID);
    }

    @Test
    void getUserNotificationsWhenUserNotFound() {
        when(userRepository.existsById(NON_EXISTENT_USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> notificationService.getUserNotifications(NON_EXISTENT_USER_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with id 999 not found");

        verify(notificationRepository, never()).findAllByUserId(any());
    }

    @Test
    void updateNotificationStatus_Success() {
        when(notificationRepository.findById(NOTIFICATION_ID)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);
        when(notificationMapper.toDto(notification)).thenReturn(notificationResponse);

        NotificationResponse result = notificationService.updateNotificationStatus(NOTIFICATION_ID, NotificationStatus.SENT);

        assertThat(result).isNotNull();
        assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);

        verify(notificationRepository).save(notification);
    }

    @Test
    void updateNotificationStatusWhenNotificationNotFound() {
        when(notificationRepository.findById(NON_EXISTENT_NOTIFICATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.updateNotificationStatus(NON_EXISTENT_NOTIFICATION_ID, NotificationStatus.SENT))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Notification with id 999 not found");

        verify(notificationRepository, never()).save(any());
    }
}