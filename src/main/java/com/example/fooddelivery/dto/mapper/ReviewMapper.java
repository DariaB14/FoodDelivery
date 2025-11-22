package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.request.ReviewRequest;
import com.example.fooddelivery.dto.response.ReviewResponse;
import com.example.fooddelivery.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true) // установим в сервисе через order
    @Mapping(target = "restaurant", ignore = true) // установим в сервисе через order
    @Mapping(target = "order", ignore = true) // установим в сервисе по orderId
    @Mapping(target = "status", constant = "PENDING") // всегда PENDING при создании
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toEntity(ReviewRequest dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    @Mapping(target = "orderId", source = "order.id")
    ReviewResponse toDto(Review review);
}
