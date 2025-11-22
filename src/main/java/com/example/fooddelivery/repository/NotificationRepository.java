package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Notification;
import com.example.fooddelivery.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserId(Long userId);
    List<Notification> findAllByStatusAndSendAtLessThanEqual(NotificationStatus status, LocalDateTime sendAt);
}
