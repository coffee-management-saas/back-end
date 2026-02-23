package com.futurenbetter.saas.modules.auth.mapper;

import com.futurenbetter.saas.modules.auth.dto.request.MembershipRankRequest;
import com.futurenbetter.saas.modules.auth.dto.response.MembershipRankResponse;
import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface MembershipRankMapper {
    MembershipRankResponse toResponse(MembershipRank membershipRank);

    MembershipRank toEntity(MembershipRankRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRankFromRequest(MembershipRankRequest request, @MappingTarget MembershipRank membershipRank);
}
