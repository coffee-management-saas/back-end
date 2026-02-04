package com.futurenbetter.saas.modules.auth.mapper;

import com.futurenbetter.saas.modules.auth.dto.response.PointHistoryResponse;
import com.futurenbetter.saas.modules.auth.entity.PointHistory;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PointHistoryMapper {

    PointHistoryResponse toResponse(PointHistory pointHistory);
}
