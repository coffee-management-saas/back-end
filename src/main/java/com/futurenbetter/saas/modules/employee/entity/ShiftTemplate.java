package com.futurenbetter.saas.modules.employee.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.LocalTime;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.employee.enums.ShiftTemplateStatus;

import jakarta.persistence.*;


@Entity
@Table(name = "shift_templates")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long shiftTemplateId;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @Column(name = "name") // nullable = false
    String name;

    @Column(name = "start_time") // nullable = false
    LocalTime startTime;

    @Column(name = "end_time") // nullable = false
    LocalTime endTime;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    ShiftTemplateStatus status;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = ShiftTemplateStatus.DRAFT;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
