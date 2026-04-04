package com.futurenbetter.saas.common.utils;

import java.util.Map;

public class PayOSUtils {
    private static final long PREFIX_SUBSCRIPTION = 1000000000000000L;// 1 - 5 - 10
    private static final long PREFIX_ORDER = 2000000000000000L; // 1 - 5 - 10

    public static long genSubscriptionCode(Long subscriptionTransactionId) {
        return PREFIX_SUBSCRIPTION
                + (subscriptionTransactionId * 10000000000L)
                + (System.currentTimeMillis() / 1000L);
    }

    public static long genOrderCode(Long shopId, Long orderId) {
        return PREFIX_ORDER
                + (shopId * 10000000000L)
                + orderId;
    }

    public static Map<Long, Long> parseOrderCode(long orderCode) {
        long shopId = (orderCode - PREFIX_ORDER) / 10000000000L;
        long orderId = orderCode - PREFIX_ORDER - (shopId * 10000000000L);
        return Map.of(1L, shopId, 2L, orderId);
    }

    public static Long parseSubscriptionCode(long subscriptionCode) {
        return (subscriptionCode - PREFIX_SUBSCRIPTION) / 10000000000L;
    }
}
