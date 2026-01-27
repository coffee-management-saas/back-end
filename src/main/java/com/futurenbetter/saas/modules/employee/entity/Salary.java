package com.futurenbetter.saas.modules.employee.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.Month;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.employee.enums.EmployeeStatus;
import com.futurenbetter.saas.modules.employee.enums.SalaryStatus;

import jakarta.persistence.*;

@Entity
@Table(name = "salaries")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Salary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long salaryId;

    @ManyToOne
    @JoinColumn(name = "shop_id", nullable = false)
    Shop shop;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    Employee employee;

    @Column(name = "month") // nullable = false
    Month month;

    @Column(name = "year") // nullable = false
    Integer year;

    @Column(name = "base_amount") // nullable = false
    Double baseAmount;

    @Column(name = "penalty_amount")
    Double penaltyAmount;

    @Column(name = "bonus_amount")
    Double bonusAmount;

    @Column(name = "actual_amount")
    Double actualAmount;

    @Column(name = "created_at", nullable = false)
    LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status") // nullable = false
    SalaryStatus status;

    @PrePersist
    public void onCreate() {
        if (status == null) {
            status = SalaryStatus.DRAFT;
        }
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
