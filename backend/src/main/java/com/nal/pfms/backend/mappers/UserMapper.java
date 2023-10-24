package com.nal.pfms.backend.mappers;

import com.nal.pfms.backend.dtos.RegisterDto;
import com.nal.pfms.backend.dtos.UserDto;
import com.nal.pfms.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toUserDto(User user);

    @Mapping(target = "password", ignore = true)
    User signUpToUser(RegisterDto registerDto);

}
