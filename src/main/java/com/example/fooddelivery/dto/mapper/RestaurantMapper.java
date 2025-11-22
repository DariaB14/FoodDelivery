package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "menu", ignore = true)
    @Mapping(target = "carts", ignore = true)
    Restaurant toEntity(RestaurantRequest dto);

    RestaurantResponse toDto(Restaurant restaurant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "menu", ignore = true)
    @Mapping(target = "carts", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void update(RestaurantRequest dto, @MappingTarget Restaurant restaurant);
}
