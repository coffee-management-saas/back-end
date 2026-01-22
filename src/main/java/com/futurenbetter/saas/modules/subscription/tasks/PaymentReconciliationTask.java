package com.futurenbetter.saas.modules.subscription.tasks;

import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionTransactionEnum;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionTransactionRepository;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentReconciliationTask {
    private final SubscriptionTransactionRepository transactionRepository;
    private final SubscriptionService subscriptionService;

    @Scheduled(fixedDelay = 900000)
    public void reconcileMissingPayment() {
        //1. Tìm các giao dịch PENDING được tạo cách đây 15 phút
        LocalDateTime limitTime = LocalDateTime.now().minusMinutes(15);
        List<SubscriptionTransaction> pendingTrans = transactionRepository.findAllByStatusAndCreatedAtBefore(
                SubscriptionTransactionEnum.PENDING, limitTime
        );

        //2. Check giao dịch thực tế có trạng thái gì?
        for (SubscriptionTransaction transaction : pendingTrans) {
            try {
                Map<String, String> momoStatus = subscriptionService.queryMomoTransaction(transaction.getOrderId());
                String resultCode = momoStatus.get("resultCode");
                if ("0".equals(resultCode)) {
                    subscriptionService.handleMomoIpn(momoStatus);
                } else {
                    transaction.setStatus(SubscriptionTransactionEnum.CANCELLED);
                    transaction.setUpdatedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                }
            } catch (Exception e) {
//                log.error("Lỗi đối soát đơn hàng {}: {}", transaction.getOrderId(), e.getMessage());
            }
        }
    }
}
