package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.dto.request.RestaurantRequest;
import com.example.fooddelivery.dto.response.RestaurantResponse;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.Restaurant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isOpen", ignore = true)
    @Mapping(target = "menu", ignore = true)
    Restaurant toEntity(RestaurantRequest dto);

    RestaurantResponse toDto(Restaurant restaurant);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rating", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isOpen", ignore = true)
    @Mapping(target = "menu", ignore = true)
    void update(RestaurantRequest dto, @MappingTarget Restaurant restaurant);

    Address toAddressEntity(AddressDto dto);
    AddressDto toAddressDto(Address address);
}
