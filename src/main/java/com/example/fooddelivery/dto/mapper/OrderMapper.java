package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.request.OrderRequest;
import com.example.fooddelivery.dto.response.OrderResponse;
import com.example.fooddelivery.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "courier", ignore = true)
    @Mapping(target = "review", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "status", constant = "NEW")
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderRequest dto);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "cartId", source = "cart.id")
    OrderResponse toDto(Order order);
}
