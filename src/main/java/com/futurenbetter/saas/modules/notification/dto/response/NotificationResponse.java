package com.futurenbetter.saas.modules.notification.dto.response;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    Long notificationId;
    String title;
    String message;
    Boolean isRead;
    String referenceLink;
    LocalDateTime createdAt;
}
