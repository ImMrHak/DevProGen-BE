package com.devprogen.application.user.mapper;

import com.devprogen.application.user.record.response.UserInformationDTO;
import com.devprogen.domain.user.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserInformationDTO UserToUserInformationDTO(User user);
}
