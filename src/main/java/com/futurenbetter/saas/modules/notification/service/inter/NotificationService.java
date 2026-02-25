package com.futurenbetter.saas.modules.notification.service.inter;

import com.futurenbetter.saas.modules.notification.dto.filter.NotificationFilter;
import com.futurenbetter.saas.modules.notification.dto.response.NotificationResponse;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import org.springframework.data.domain.Page;

public interface NotificationService {
    void sendToUser(Notification notification);
    void sendToShopRole(Notification notification, String roleName);
    Page<NotificationResponse> getAll(NotificationFilter filter);
    NotificationResponse markAsRead(Long notificationId);
}
