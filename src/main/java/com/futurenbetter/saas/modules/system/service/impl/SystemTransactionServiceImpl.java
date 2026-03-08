package com.futurenbetter.saas.modules.system.service.impl;

import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import com.futurenbetter.saas.modules.system.dto.filter.SystemTransactionFilter;
import com.futurenbetter.saas.modules.system.dto.request.SystemTransactionRequest;
import com.futurenbetter.saas.modules.system.dto.response.SystemTransactionResponse;
import com.futurenbetter.saas.modules.system.entity.SystemTransaction;
import com.futurenbetter.saas.modules.system.mapper.SystemTransactionMapper;
import com.futurenbetter.saas.modules.system.repository.SystemTransactionRepository;
import com.futurenbetter.saas.modules.system.service.inter.SystemTransactionService;
import com.futurenbetter.saas.modules.system.spec.SystemTransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemTransactionServiceImpl implements SystemTransactionService {

    private final SystemTransactionRepository systemTransactionRepository;
    private final SystemTransactionMapper systemTransactionMapper;

    @Override
    @Transactional
    public SystemTransactionResponse create(SystemTransactionRequest systemTransactionRequest) {

        UserProfile currentUser = SecurityUtils.getCurrentUserProfile();

        SystemTransaction result = systemTransactionMapper
                .toEntity(systemTransactionRequest, currentUser);
        result = systemTransactionRepository.save(result);

        return systemTransactionMapper.toResponse(result);
    }

    @Override
    public Page<SystemTransactionResponse> findAll(SystemTransactionFilter systemTransactionFilter) {
        return systemTransactionRepository.findAll(
                SystemTransactionSpecification.filter(systemTransactionFilter),
                systemTransactionFilter.getPageable()
        ).map(systemTransactionMapper::toResponse);
    }
}
