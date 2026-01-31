package com.futurenbetter.saas.modules.employee.service.impl;

import com.futurenbetter.saas.common.dto.request.BaseFilter;
import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.employee.dto.request.ShiftTemplateRequest;
import com.futurenbetter.saas.modules.employee.dto.response.ShiftTemplateResponse;
import com.futurenbetter.saas.modules.employee.entity.ShiftTemplate;
import com.futurenbetter.saas.modules.employee.enums.ShiftTemplateStatus;
import com.futurenbetter.saas.modules.employee.mapper.ShiftTemplateMapper;
import com.futurenbetter.saas.modules.employee.repository.ShiftTemplateRepository;
import com.futurenbetter.saas.modules.employee.service.inter.ShiftTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShiftTemplateServiceImpl implements ShiftTemplateService {

    private final ShiftTemplateRepository shiftTemplateRepository;
    private final ShiftTemplateMapper shiftTemplateMapper;


    @Override
    @Transactional
    public ShiftTemplateResponse create(ShiftTemplateRequest request) {

        Shop currentShop = SecurityUtils.getCurrentShop();

        ShiftTemplate template = shiftTemplateMapper.toEntity(request);
        template.setShop(currentShop);
        template.setStatus(ShiftTemplateStatus.ACTIVE);

        ShiftTemplate savedTemplate = shiftTemplateRepository.save(template);

        return shiftTemplateMapper.toResponse(savedTemplate);
    }


    @Override
    @Transactional
    public ShiftTemplateResponse update(Long id, ShiftTemplateRequest request) {

        Long shopId = TenantContext.getCurrentShopId();

        ShiftTemplate template = shiftTemplateRepository.findByShiftTemplateIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Mẫu ca làm việc không tồn tại"));

        shiftTemplateMapper.updateFromRequest(request, template);

        ShiftTemplate updatedTemplate = shiftTemplateRepository.save(template);

        return shiftTemplateMapper.toResponse(updatedTemplate);
    }


    @Override
    public ShiftTemplateResponse getDetail(Long id) {

        Long shopId = TenantContext.getCurrentShopId();

        return shiftTemplateRepository.findByShiftTemplateIdAndShopId(id, shopId)
                .map(shiftTemplateMapper::toResponse)
                .orElseThrow(() -> new BusinessException("Mẫu ca làm việc không tồn tại"));
    }


    @Override
    @Transactional
    public void delete(Long id) {

        Long shopId = TenantContext.getCurrentShopId();
        ShiftTemplate template = shiftTemplateRepository.findByShiftTemplateIdAndShopId(id, shopId)
                .orElseThrow(() -> new BusinessException("Mẫu ca làm việc không tồn tại"));

        template.setStatus(ShiftTemplateStatus.INACTIVE);
        shiftTemplateRepository.save(template);
    }


    @Override
    public Page<ShiftTemplateResponse> getAll(BaseFilter filter) {

        Long shopId = TenantContext.getCurrentShopId();

        return shiftTemplateRepository.findAllByShopId(shopId, filter.getPageable())
                .map(shiftTemplateMapper::toResponse);
    }
}