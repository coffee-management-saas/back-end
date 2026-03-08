package com.futurenbetter.saas.modules.employee.entity;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.employee.enums.UnavailabilityStatus;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "employee_unavailabilities")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeUnavailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long employeeUnavailabilityId;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @Column(name = "day_of_week") // nullable = false
    DayOfWeek dayOfWeek;

    @Column(name = "start_time") // nullable = false
    LocalDateTime startTime;

    @Column(name = "end_time") // nullable = false
    LocalDateTime endTime;

    @Column(name = "specific_date")
    LocalDateTime specificDate;

    @Column(name = "reason")
    String reason;

    @Column(name = "is_recurring") // nullable = false
    Boolean isRecurring;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Column(name = "status") // nullable = false
    UnavailabilityStatus status;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = UnavailabilityStatus.ACTIVE;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
