package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.request.CartItemRequest;
import com.example.fooddelivery.dto.request.CartRequest;
import com.example.fooddelivery.dto.response.CartItemResponse;
import com.example.fooddelivery.dto.response.CartResponse;
import com.example.fooddelivery.entity.Cart;
import com.example.fooddelivery.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "restaurantId", source = "restaurant.id")
    CartResponse toDto(Cart cart);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "restaurant", ignore = true) 
    @Mapping(target = "eta", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cart toEntity(CartRequest dto);
}
