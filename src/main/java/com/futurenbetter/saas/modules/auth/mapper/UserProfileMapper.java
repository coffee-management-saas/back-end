package com.futurenbetter.saas.modules.auth.mapper;

import com.futurenbetter.saas.modules.auth.dto.request.SystemAdminRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.response.ShopEmployeeRegistrationResponse;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminRegistrationResponse;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeResponse;
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

    @Mapping(target = "employee", source = "employeeResponse")
    @Mapping(target = "userProfileId", source = "userProfile.userProfileId")
    @Mapping(target = "username", source = "userProfile.username")
    @Mapping(target = "fullname", source = "userProfile.fullname")
    @Mapping(target = "email", source = "userProfile.email")
    @Mapping(target = "phone", source = "userProfile.phone")
    @Mapping(target = "address", source = "userProfile.address")
    @Mapping(target = "dob", source = "userProfile.dob")
    @Mapping(target = "createdAt", source = "userProfile.createdAt")
    ShopEmployeeRegistrationResponse toEmployeeResponse(UserProfile userProfile, EmployeeResponse employeeResponse);
}
