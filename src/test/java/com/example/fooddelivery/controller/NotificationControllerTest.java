package com.example.fooddelivery.controller;

import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.response.NotificationResponse;
import com.example.fooddelivery.enums.NotificationChannel;
import com.example.fooddelivery.enums.NotificationStatus;
import com.example.fooddelivery.enums.NotificationType;
import com.example.fooddelivery.exception.EntityNotFoundException;
import com.example.fooddelivery.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    private NotificationRequest notificationRequest;
    private NotificationResponse notificationResponse;

    private static final String BASE_URL = "/notifications";

    @BeforeEach
    void setUp() {
        notificationRequest = new NotificationRequest(1L, "Заказ готов к выдаче", NotificationType.ORDER_READY, NotificationChannel.PUSH, LocalDateTime.now());
        notificationResponse = new NotificationResponse(1L, 1L, "Заказ готов к выдаче", NotificationType.ORDER_READY, NotificationStatus.PENDING, NotificationChannel.PUSH, LocalDateTime.now(), null);
    }

    @Test
    void createNotification_Success() throws Exception {
        when(notificationService.createNotification(any(NotificationRequest.class))).thenReturn(notificationResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("invalidNotificationData")
    void createNotificationWithInvalidData(String testName, NotificationRequest invalidRequest) throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> invalidNotificationData() {
        return Stream.of(
                Arguments.of("nullUserId",
                        new NotificationRequest(null, "Заказ готов к выдаче", NotificationType.ORDER_READY, NotificationChannel.PUSH, LocalDateTime.now())),
                Arguments.of("emptyMessage",
                        new NotificationRequest(1L, "", NotificationType.ORDER_READY, NotificationChannel.PUSH, LocalDateTime.now())),
                Arguments.of("nullType",
                        new NotificationRequest(1L, "Заказ готов к выдаче", null, NotificationChannel.PUSH, LocalDateTime.now())),
                Arguments.of("nullChannel",
                        new NotificationRequest(1L, "Заказ готов к выдаче", NotificationType.ORDER_READY, null, LocalDateTime.now()))
        );
    }

    @Test
    void createNotification_WhenUserNotFound() throws Exception {
        when(notificationService.createNotification(any(NotificationRequest.class))).thenThrow(new EntityNotFoundException("User with id 100 not found"));

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notificationRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserNotifications_Success() throws Exception {
        Long userId = 1L;
        List<NotificationResponse> notifications = List.of(
                new NotificationResponse(1L, userId, "Заказ ожидает оплаты", NotificationType.ORDER_CREATED,
                        NotificationStatus.SENT, NotificationChannel.PUSH, LocalDateTime.now(), LocalDateTime.now()),
                new NotificationResponse(2L, userId, "Курьер в пути", NotificationType.ORDER_DELIVERING,
                        NotificationStatus.DELIVERED, NotificationChannel.PUSH, LocalDateTime.now(), LocalDateTime.now()));

        when(notificationService.getUserNotifications(userId)).thenReturn(notifications);

        mockMvc.perform(get(BASE_URL)
                        .param("userId", userId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getUserNotificationsWhenUserNotFound() throws Exception {
        Long userId = 100L;
        when(notificationService.getUserNotifications(userId)).thenThrow(new EntityNotFoundException("User with id 100 not found"));

        mockMvc.perform(get(BASE_URL)
                        .param("userId", userId.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserNotificationsWithoutParam() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNotificationStatus_Success() throws Exception {
        Long notificationId = 1L;
        NotificationStatus newStatus = NotificationStatus.SENT;

        NotificationResponse updatedResponse = new NotificationResponse(
                notificationId, 1L, "Заказ готов к выдаче", NotificationType.ORDER_READY,
                newStatus, NotificationChannel.PUSH, LocalDateTime.now(), LocalDateTime.now());

        when(notificationService.updateNotificationStatus(notificationId, newStatus)).thenReturn(updatedResponse);

        mockMvc.perform(patch(BASE_URL + "/{id}/status", notificationId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void updateNotificationStatusWhenNotificationNotFound() throws Exception {
        Long notificationId = 100L;
        NotificationStatus newStatus = NotificationStatus.SENT;

        when(notificationService.updateNotificationStatus(notificationId, newStatus)).thenThrow(new EntityNotFoundException("Notification with id 100 not found"));

        mockMvc.perform(patch(BASE_URL + "/{id}/status", notificationId)
                        .param("status", newStatus.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateNotificationStatusWithoutParam() throws Exception {
        Long notificationId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/status", notificationId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateNotificationStatus_WithInvalidParam() throws Exception {
        Long notificationId = 1L;

        mockMvc.perform(patch(BASE_URL + "/{id}/status", notificationId)
                        .param("status", "invalid"))
                .andExpect(status().isBadRequest());
    }
}