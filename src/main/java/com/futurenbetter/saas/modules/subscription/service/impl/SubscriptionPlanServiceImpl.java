package com.futurenbetter.saas.modules.subscription.service.impl;

import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionPlanRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionPlanResponse;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionPlan;
import com.futurenbetter.saas.modules.subscription.enums.SubscriptionPlanEnum;
import com.futurenbetter.saas.modules.subscription.mapper.SubscriptionPlanMapper;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionPlanRepository;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanMapper subscriptionPlanMapper;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public SubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanRequest) {
        SubscriptionPlan plan = subscriptionPlanMapper.toEntity(subscriptionPlanRequest);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        plan.setSubscriptionPlanStatus(SubscriptionPlanEnum.ACTIVE);

        SubscriptionPlan saved = subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toResponse(saved);
    }

    @Override
    public List<SubscriptionPlanResponse> getAllSubscriptionPlan() {
        return subscriptionPlanRepository.findAllBySubscriptionPlanStatus(SubscriptionPlanEnum.ACTIVE)
                .stream()
                .map(subscriptionPlanMapper::toResponse)
                .toList();
    }
}
