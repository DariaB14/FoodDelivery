package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.request.NotificationRequest;
import com.example.fooddelivery.dto.response.NotificationResponse;
import com.example.fooddelivery.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // установим в сервисе по userId
    @Mapping(target = "status", constant = "PENDING") // всегда PENDING при создании
    @Mapping(target = "sentAt", ignore = true)
    Notification toEntity(NotificationRequest dto);

    @Mapping(target = "userId", source = "user.id")
    NotificationResponse toDto(Notification notification);
}
