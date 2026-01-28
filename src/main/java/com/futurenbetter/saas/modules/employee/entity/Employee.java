package com.futurenbetter.saas.modules.employee.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import com.futurenbetter.saas.modules.employee.enums.EmployeeStatus;
import com.futurenbetter.saas.modules.employee.enums.EmployeeType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long employeeId;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;



    @OneToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    UserProfile userProfile;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_type") // nullable = false
    EmployeeType employeeType;

    @Column(name = "hourly_wage") // nullable = false
    Double hourlyWage;

    @Column(name = "weekly_hour_limit")
    Double weeklyHourLimit; // giới hạn số h làm torng 1 tuần, dùng cho parttime

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    EmployeeStatus status;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = EmployeeStatus.ACTIVE;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
