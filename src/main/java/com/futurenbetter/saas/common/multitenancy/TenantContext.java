package com.futurenbetter.saas.common.multitenancy;

public class TenantContext {
    private static final ThreadLocal<Long> CURRENT_SHOP_ID = new ThreadLocal<>();

    public static void setCurrentShopId(Long shopId){
        CURRENT_SHOP_ID.set(shopId);
    }

    public static Long getCurrentShopId(){
        return CURRENT_SHOP_ID.get();
    }

    public static void clearCurrentShopId(){
        CURRENT_SHOP_ID.remove();
    }
}
