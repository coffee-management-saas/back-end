package com.futurenbetter.saas.modules.employee.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.employee.enums.TimeEntryStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "time_entries")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long timeEntryId;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    Schedule schedule;

    @Column(name = "check_in_at") // nullable = false
    LocalDateTime checkInAt;

    @Column(name = "check_out_at")
    LocalDateTime checkOutAt;

    @Column(name = "approved_hours")
    Double approvedHours;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    TimeEntryStatus status;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = TimeEntryStatus.PENDING;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
