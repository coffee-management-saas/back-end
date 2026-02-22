package com.futurenbetter.saas.modules.notification.service.impl;

import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.repository.NotificationRepository;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void sendToUser(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);

        // queue/notifications/{recipientType}/{recipientId}
        String destination = String.format("/queue/notifications/%s/%d",
                savedNotification.getRecipientType(),
                savedNotification.getRecipientId());

        messagingTemplate.convertAndSend(destination, savedNotification);
    }

    @Transactional
    public void sendToShopRole(Notification notification, String roleName) {
        Notification savedNotification = notificationRepository.save(notification);

        // topic/shop/1/role/SHOP_MANAGER
        String destination = String.format("/topic/shop/%d/role/%s",
                savedNotification.getShop().getId(), roleName);

        messagingTemplate.convertAndSend(destination, savedNotification);
    }
}
