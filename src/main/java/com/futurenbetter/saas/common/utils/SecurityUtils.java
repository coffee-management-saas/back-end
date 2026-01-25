package com.futurenbetter.saas.common.utils;

import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class SecurityUtils {
    public static Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Customer) {
            return (Customer) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        Customer customer = getCurrentCustomer();
        return (customer != null) ? customer.getId() : null;
    }

    public static Long getCurrentShopId() {
        Shop shop = getCurrentShop();
        return (shop != null) ? shop.getId() : null;
    }

    public static Shop getCurrentShop() {
        Customer customer = getCurrentCustomer();
        return (customer != null) ? customer.getShop() : null;
    }
}
