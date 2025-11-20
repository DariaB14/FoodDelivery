package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.request.ItemOptionRequest;
import com.example.fooddelivery.dto.request.ItemRequest;
import com.example.fooddelivery.dto.response.ItemOptionResponse;
import com.example.fooddelivery.dto.response.ItemResponse;
import com.example.fooddelivery.entity.Item;
import com.example.fooddelivery.entity.ItemOption;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ItemMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "cartItems", ignore = true)
    Item toEntity(ItemRequest dto);

    @Mapping(target = "restaurantId", source = "restaurant.id")
    ItemResponse toDto(Item item);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "options", ignore = true)
    void update(ItemRequest dto, @MappingTarget Item item);

    ItemOption toOptionEntity(ItemOptionRequest dto);
    ItemOptionResponse toOptionDto(ItemOption option);

    List<ItemOption> toOptionEntities(List<ItemOptionRequest> dtos);
    List<ItemOptionResponse> toOptionDtos(List<ItemOption> options);
}
