package com.futurenbetter.saas.modules.dashboard.service.impl;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import com.futurenbetter.saas.modules.dashboard.dto.filter.DashboardFilter;
import com.futurenbetter.saas.modules.dashboard.repository.SystemMonthlyReportRepository;
import com.futurenbetter.saas.modules.dashboard.service.inter.SystemDashboardService;
import com.futurenbetter.saas.modules.subscription.repository.SubscriptionTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SystemDashboardServiceImpl implements SystemDashboardService {

    private final SubscriptionTransactionRepository transactionRepository;
    private final ShopRepository shopRepository;
    private final SystemMonthlyReportRepository monthlyReportRepository;


}
