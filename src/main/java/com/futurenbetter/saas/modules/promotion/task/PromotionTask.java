package com.futurenbetter.saas.modules.promotion.task;

import com.futurenbetter.saas.modules.promotion.entity.Promotion;
import com.futurenbetter.saas.modules.promotion.enums.PromotionEnum;
import com.futurenbetter.saas.modules.promotion.repository.PromotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PromotionTask {
    private final PromotionRepository promotionRepository;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void deactivateExpiredPromotions() {
        List<Promotion> expiredPromotion = promotionRepository.findExpiredPromotions(
                PromotionEnum.ACTIVE,
                LocalDateTime.now()
        );

        if (expiredPromotion.isEmpty()) {
            expiredPromotion.forEach(p -> {
                p.setPromotionStatus(PromotionEnum.INACTIVE);
                p.setUpdatedAt(LocalDateTime.now());
            });
            promotionRepository.saveAll(expiredPromotion);
        }
    }
}
