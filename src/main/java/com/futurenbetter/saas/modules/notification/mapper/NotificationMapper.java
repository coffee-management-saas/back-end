package com.futurenbetter.saas.modules.notification.mapper;

import com.futurenbetter.saas.modules.notification.dto.response.NotificationResponse;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface NotificationMapper {
    @Mapping(target = "shopId", source = "shop.id")
//    @Mapping(target = "isRead", source = "isRead")
    NotificationResponse toResponse(Notification notification);
}
