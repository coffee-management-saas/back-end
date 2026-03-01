package com.futurenbetter.saas.modules.dashboard.dto.projection;

public interface BestSellerProjection {
    Long getProductId();
    String getProductName();
    String getProductImage();
    Long getTotalQuantity();
    Long getTotalRevenue();
    Long getPrice();
}
