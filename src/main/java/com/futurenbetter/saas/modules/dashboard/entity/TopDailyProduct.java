package com.futurenbetter.saas.modules.dashboard.entity;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "top_daily_products",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {
                        "shop_id",
                        "report_date",
                        "product_id"
                })
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopDailyProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_sold")
    private Integer quantitySold;
}
