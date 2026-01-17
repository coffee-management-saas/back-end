package com.futurenbetter.saas.modules.subscription.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.ShopStatus;
import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionPlanRequest;
import com.futurenbetter.saas.modules.subscription.dto.request.SubscriptionRequest;
import com.futurenbetter.saas.modules.subscription.dto.response.MomoPaymentResponse;
import com.futurenbetter.saas.modules.subscription.dto.response.SubscriptionPlanResponse;
import com.futurenbetter.saas.modules.subscription.entity.BillingInvoice;
import com.futurenbetter.saas.modules.subscription.entity.ShopSubscription;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionPlan;
import com.futurenbetter.saas.modules.subscription.entity.SubscriptionTransaction;
import com.futurenbetter.saas.modules.subscription.enums.*;
import com.futurenbetter.saas.modules.subscription.mapper.SubscriptionPlanMapper;
import com.futurenbetter.saas.modules.subscription.repository.BillingInvoiceRepository;
import com.futurenbetter.saas.modules.subscription.repository.ShopSubscriptionRepository;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionPlanRepository;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionTransactionRepository;
import com.futurenbetter.saas.modules.subscription.service.SubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.*;

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

    @Override
    public SubscriptionPlanResponse getSubscriptionPlanById(Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Gói dịch vụ không tồn tại"));
    return subscriptionPlanMapper.toResponse(plan);
    }

    @Override
    public SubscriptionPlanResponse updateSubscriptionPlan(SubscriptionPlanRequest subscriptionPlanRequest, Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Gói dịch vụ không tồn tại"));

        subscriptionPlanMapper.updateEntityFromRequest(subscriptionPlanRequest, plan);
        plan.setUpdatedAt(LocalDateTime.now());
        SubscriptionPlan saved = subscriptionPlanRepository.save(plan);
        return subscriptionPlanMapper.toResponse(saved);
    }

    @Override
    public void deleteSubscriptionPlan(Long id) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Gói dịch vụ không tồn tại"));

        plan.setSubscriptionPlanStatus(SubscriptionPlanEnum.INACTIVE);
        plan.setUpdatedAt(LocalDateTime.now());
        subscriptionPlanRepository.save(plan);
    }
}
