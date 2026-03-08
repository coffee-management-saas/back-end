package com.futurenbetter.saas.modules.notification.dto.response;

import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.nio.Buffer;
import java.time.LocalDateTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long notificationId;
    Long recipientId;
    String title;
    String message;
    Boolean isRead;
    String referenceLink;
    String recipientType;
    NotificationType type;
    Long shopId;
    LocalDateTime createdAt;
}
