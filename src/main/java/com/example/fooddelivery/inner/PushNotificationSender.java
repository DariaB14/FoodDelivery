package com.example.fooddelivery.inner;

import com.example.fooddelivery.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PushNotificationSender implements NotificationSender {
    @Override
    public void send(Notification notification) throws Exception {
        log.info("Push sent to user {}, {}", notification.getUser().getId(), notification.getMessage());
    }
}
