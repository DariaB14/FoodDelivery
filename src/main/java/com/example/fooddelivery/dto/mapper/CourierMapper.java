package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.request.CourierRequest;
import com.example.fooddelivery.dto.response.CourierResponse;
import com.example.fooddelivery.entity.Courier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourierMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "OFFLINE")
    @Mapping(target = "rating", constant = "0")
    @Mapping(target = "currentOrdersAmount", constant = "0")
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Courier toEntity(CourierRequest dto);

    CourierResponse toDto(Courier courier);
}
