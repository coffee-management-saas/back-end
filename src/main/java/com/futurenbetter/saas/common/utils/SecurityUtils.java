package com.futurenbetter.saas.common.utils;

import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// update lại để ktra là dùng từ customer hay userprofile
public class SecurityUtils {

    public static Customer getCurrentCustomer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Customer) {
            return (Customer) authentication.getPrincipal();
        }
        return null;
    }


    public static UserProfile getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserProfile) {
            return (UserProfile) authentication.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        Customer customer = getCurrentCustomer();
        if (customer != null) {
            return customer.getId();
        }

        UserProfile userProfile = getCurrentUserProfile();
        if (userProfile != null) {
            return userProfile.getUserProfileId();
        }

        return null;
    }

    public static Long getCurrentShopId() {
        Shop shop = getCurrentShop();
        return (shop != null) ? shop.getId() : null;
    }

    public static Shop getCurrentShop() {
        Customer customer = getCurrentCustomer();
        if (customer != null) {
            return customer.getShop();
        }

        UserProfile userProfile = getCurrentUserProfile();
        if (userProfile != null) {
            return userProfile.getShop();
        }

        return null;
    }

    public static boolean isCustomer() {
        return getCurrentCustomer() != null;
    }

    public static boolean isUserProfile() {
        return getCurrentUserProfile() != null;
    }
}