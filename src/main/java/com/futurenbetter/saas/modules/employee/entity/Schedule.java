package com.futurenbetter.saas.modules.employee.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.employee.enums.ScheduleStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "schedules")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;
    // có thể thêm các field: dayOfWeek, Month phục vụ cho select.

    @Column(name = "date") // nullable = false
    LocalDate date;

    @Column(name = "start_time") // nullable = false
    LocalDateTime startTime;

    @Column(name = "end_time") // nullable = false
    LocalDateTime endTime;

    @Column(name = "task")
    String task;

    @Column(name = "is_recurring") // nullable = false
    Boolean isRecurring;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    ScheduleStatus status;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = ScheduleStatus.DRAFT;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
