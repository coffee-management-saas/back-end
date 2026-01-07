package com.futurenbetter.saas.modules.inventory.service.inter;

import com.futurenbetter.saas.modules.inventory.dto.filter.StockCheckSessionFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckApproveRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckStartRequest;
import com.futurenbetter.saas.modules.inventory.dto.request.StockCheckUpdateRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.StockCheckSessionResponse;
import org.springframework.data.domain.Page;

public interface StockCheckService {
    StockCheckSessionResponse startSession(StockCheckStartRequest request);
    StockCheckSessionResponse updateCount(StockCheckUpdateRequest request);
    StockCheckSessionResponse approveSession(StockCheckApproveRequest request);
    Page<StockCheckSessionResponse> getAll(StockCheckSessionFilter filter);
}
