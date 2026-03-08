package com.futurenbetter.saas.modules.system.mapper;

import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import com.futurenbetter.saas.modules.system.dto.request.SystemTransactionRequest;
import com.futurenbetter.saas.modules.system.dto.response.SystemTransactionResponse;
import com.futurenbetter.saas.modules.system.entity.SystemTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface SystemTransactionMapper {
    @Mapping(target = "approverId", source = "approver.userProfileId")
    SystemTransactionResponse toResponse(SystemTransaction systemTransaction);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "approver", source = "userProfile")
    SystemTransaction toEntity(SystemTransactionRequest systemTransactionRequest, UserProfile userProfile);
}
