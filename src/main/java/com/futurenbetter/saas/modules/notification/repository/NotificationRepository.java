package com.futurenbetter.saas.modules.notification.repository;

import com.futurenbetter.saas.modules.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
