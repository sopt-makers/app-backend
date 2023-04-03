package org.sopt.app.presentation.user;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.sopt.app.application.user.UserInfo;
import org.sopt.app.domain.entity.User;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface UserResponseMapper {

    UserResponse.AppUser ofAppUser(User user);

    UserResponse.Main ofMainView(UserResponse.User user, UserResponse.Operation operation);

    UserResponse.Soptamp ofSoptamp(User user);

    UserResponse.Nickname of(UserInfo.Nickname nickname);

    UserResponse.ProfileMessage of(UserInfo.ProfileMessage profileMessage);
}
