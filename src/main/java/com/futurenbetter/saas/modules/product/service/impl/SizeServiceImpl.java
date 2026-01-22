package com.futurenbetter.saas.modules.product.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.product.dto.request.SizeRequest;
import com.futurenbetter.saas.modules.product.entity.Size;
import com.futurenbetter.saas.modules.product.enums.Status;
import com.futurenbetter.saas.modules.product.mapper.SizeMapper;
import com.futurenbetter.saas.modules.product.repository.SizeRepository;
import com.futurenbetter.saas.modules.product.service.inter.SizeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SizeServiceImpl implements SizeService {

    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;

    @Override
    @Transactional
    public Size create(SizeRequest request) {
        Long shopId = SecurityUtils.getCurrentShopId();
        if (sizeRepository.existsByCodeAndShopId(request.getCode(), shopId)) {
            throw new BusinessException("Mã kích thước đã tồn tại");
        }

        Size size = sizeMapper.toEntity(request);
        size.setShop(SecurityUtils.getCurrentShop());

        return sizeRepository.save(size);
    }

    @Override
    @Transactional
    public Size update(Long id, SizeRequest request) {
        Size size = sizeRepository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Kích thước không tồn tại"));

        if (request.getCode() != null && !request.getCode().equals(size.getCode())) {
            if (sizeRepository.existsByCodeAndShopId(request.getCode(), SecurityUtils.getCurrentShopId())) {
                throw new BusinessException("Mã kích thước đã tồn tại");
            }
        }

        sizeMapper.updateFromRequest(size, request);
        return sizeRepository.save(size);
    }

    @Override
    public Page<Size> getAll(BaseFilter filter) {
        return sizeRepository.findAll(filter.getPageable());
    }

    @Override
    public List<Size> getActiveSizes() {
        return sizeRepository.findAll();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Size size = sizeRepository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Kích thước không tồn tại"));

        size.setStatus(Status.DELETED);
        sizeRepository.save(size);
    }
}