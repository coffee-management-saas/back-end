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
//        log.info("Bắt đầu tiến trình đối soát giao dịch treo...");

        //1. Tìm các giao dịch PENDING được tạo cách đây 15 phút
        LocalDateTime limitTime = LocalDateTime.now().minusHours(2);
        List<SubscriptionTransaction> pendingTrans = transactionRepository.findAllByStatusAndCreatedAtBefore(
                SubscriptionTransactionEnum.PENDING, limitTime
        );

        //2. Check giao dịch thực tế có trạng thái gì?
        for (SubscriptionTransaction transaction : pendingTrans) {
            try {
                Map<String, String> momoStatus = subscriptionService.queryMomoTransaction(transaction.getOrderId());
                if (momoStatus != null && "0".equals(String.valueOf(momoStatus.get("resultCode")))) {
                    // 3. Nếu Momo báo THÀNH CÔNG, tiến hành bù dữ liệu (Tạo Shop, Invoice...)
//                    log.info("Phát hiện giao dịch {} thành công trên Momo nhưng DB chưa cập nhật. Đang bù dữ liệu...", transaction.getOrderId());
                    subscriptionService.handleMomoIpn(momoStatus);
                } else {
//                    log.info("Giao dịch {} vẫn chưa hoàn tất hoặc thất bại trên Momo.", transaction.getOrderId());
                }
            } catch (Exception e) {
//                log.error("Lỗi khi đối soát đơn hàng {}: {}", transaction.getOrderId(), e.getMessage());
            }
        }
    }
}
