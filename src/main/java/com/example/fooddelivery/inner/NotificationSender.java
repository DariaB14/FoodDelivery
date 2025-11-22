package com.example.fooddelivery.inner;

import com.example.fooddelivery.entity.Notification;


public interface NotificationSender {
    void send(Notification notification) throws Exception;
}
