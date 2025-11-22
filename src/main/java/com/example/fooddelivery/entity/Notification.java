package com.example.fooddelivery.entity;

import com.example.fooddelivery.enums.NotificationChannel;
import com.example.fooddelivery.enums.NotificationStatus;
import com.example.fooddelivery.enums.NotificationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public Notification(User user, String message, NotificationType type, NotificationStatus status, NotificationChannel channel) {
        this.user = user;
        this.message = message;
        this.type = type;
        this.status = status;
        this.channel = channel;
    }
}
