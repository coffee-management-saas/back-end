package com.futurenbetter.saas.modules.notification.mapper;

import com.futurenbetter.saas.modules.notification.dto.response.NotificationResponse;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}
