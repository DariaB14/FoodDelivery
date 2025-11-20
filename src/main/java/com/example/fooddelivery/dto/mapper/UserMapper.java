package com.example.fooddelivery.dto.mapper;

import com.example.fooddelivery.dto.AddressDto;
import com.example.fooddelivery.dto.request.UserRequest;
import com.example.fooddelivery.dto.response.UserResponse;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", constant = "true")
    User toEntity(UserRequest dto);

    UserResponse toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void update(UserRequest dto, @MappingTarget User user);

    Address toAddressEntity(AddressDto dto);
    AddressDto toAddressDto(Address address);
}
