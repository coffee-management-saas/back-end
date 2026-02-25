package com.futurenbetter.saas.modules.notification.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long notificationId;

    @Column(nullable = false)
    String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    NotificationType type;

    @Column(name = "recipient_type", nullable = false)
    String recipientType; // "CUSTOMER", "SHOP", "EMPLOYEE", "SYSTEM"

    @Column(name = "recipient_id")
    Long recipientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    Shop shop;

    // Link để khi click vào thông báo trên UI thì chuyển hướng trang
    @Column(name = "reference_link")
    String referenceLink; // "/orders/123" hoặc "/inventory/stock-check/45"

    @Column(name = "is_read")
    boolean isRead = false;

    @Column(name = "created_at")
    LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
