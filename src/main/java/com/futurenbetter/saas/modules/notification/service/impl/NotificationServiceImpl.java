package com.futurenbetter.saas.modules.notification.service.impl;

import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.multitenancy.TenantFilter;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.notification.dto.filter.NotificationFilter;
import com.futurenbetter.saas.modules.notification.dto.response.NotificationResponse;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.mapper.NotificationMapper;
import com.futurenbetter.saas.modules.notification.repository.NotificationRepository;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import com.futurenbetter.saas.modules.notification.specification.NotificationSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void sendToUser(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);

        // queue/notifications/{recipientType}/{recipientId}
        String destination = String.format("/queue/notifications/%s/%d",
                savedNotification.getRecipientType(),
                savedNotification.getRecipientId());

        NotificationResponse notificationResponse = notificationMapper.toResponse(savedNotification);

        messagingTemplate.convertAndSend(destination, notificationResponse);
    }

    @Transactional
    public void sendToShopRole(Notification notification, String roleName) {
        Notification savedNotification = notificationRepository.save(notification);

        // topic/shop/1/role/SHOP_MANAGER
        String destination = String.format("/topic/shop/%d/role/%s",
                savedNotification.getShop().getId(), roleName);

        messagingTemplate.convertAndSend(destination, savedNotification);
    }

    @Override
    public Page<NotificationResponse> getAll(NotificationFilter filter) {

        Long shopId = SecurityUtils.getCurrentShopId();
        Long recipientId = SecurityUtils.getCurrentUserId();

        System.out.println("Shop ID: " + shopId);
        System.out.println("Recipient ID: " + recipientId);

        return notificationRepository.findAll(
                NotificationSpecification.filter(filter, shopId, recipientId),
                        filter.getPageable()
                ).map(notificationMapper::toResponse);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);

        Notification result = notificationRepository.save(notification);

        return notificationMapper.toResponse(result);
    }
}
