package com.futurenbetter.saas.modules.system.service.inter;

import com.futurenbetter.saas.modules.system.dto.filter.SystemTransactionFilter;
import com.futurenbetter.saas.modules.system.dto.request.SystemTransactionRequest;
import com.futurenbetter.saas.modules.system.dto.response.SystemTransactionResponse;
import org.springframework.data.domain.Page;


public interface SystemTransactionService {
    SystemTransactionResponse create(SystemTransactionRequest systemTransactionRequest);
    Page<SystemTransactionResponse> findAll(SystemTransactionFilter systemTransactionFilter);
}
