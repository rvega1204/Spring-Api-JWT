package com.rvg.store.mappers;

import com.rvg.store.dtos.RegisterUserRequest;
import com.rvg.store.dtos.UpdateUserRequest;
import com.rvg.store.dtos.UserDto;
import com.rvg.store.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    UserDto toDto(User user);

    User toEntity(RegisterUserRequest registerUserRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // Use change-password endpoint
    void update(UpdateUserRequest request, @MappingTarget User user);
}
