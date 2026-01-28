package com.futurenbetter.saas.modules.product.service.inter;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.modules.product.dto.request.SizeRequest;
import com.futurenbetter.saas.modules.product.dto.response.SizeResponse;
import com.futurenbetter.saas.modules.product.entity.Size;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SizeService {
    SizeResponse create(SizeRequest request);
    Size update(Long id, SizeRequest request);
    Page<Size> getAll(BaseFilter filter);
    List<Size> getActiveSizes();
    void delete(Long id);
}