package com.futurenbetter.saas.modules.auth.mapper;

import com.futurenbetter.saas.modules.auth.dto.request.SystemAdminRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminRegistrationResponse;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    @Mapping(target = "userProfileId", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "password", ignore = true)
    UserProfile toEntity(SystemAdminRegistrationRequest request);

    SystemAdminRegistrationResponse toAdminResponse(UserProfile userProfile);
}
