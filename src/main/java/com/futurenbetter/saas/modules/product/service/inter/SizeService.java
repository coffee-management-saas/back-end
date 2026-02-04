package com.futurenbetter.saas.modules.product.service.inter;

import com.futurenbetter.saas.modules.product.dto.request.SizeRequest;
import com.futurenbetter.saas.modules.product.dto.response.SizeResponse;
import com.futurenbetter.saas.modules.product.entity.Size;
import com.futurenbetter.saas.modules.product.enums.SizeStatus;
import com.futurenbetter.saas.modules.product.enums.Status;

import java.util.List;

public interface SizeService {
    SizeResponse create(SizeRequest request);
    Size update(Long id, SizeRequest request);
    List<SizeResponse> getAll(SizeStatus status);
    List<Size> getActiveSizes();
    void delete(Long id);
}