package com.module.purchase.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.module.purchase.entity.Users;
import com.module.purchase.entityDTO.UsersDTO;

@Mapper(componentModel="spring")
public interface UsersMapper {

    UsersDTO toUserDTO(Users users);

    List<UsersDTO> toUserDTOList(List<Users> users);
}
