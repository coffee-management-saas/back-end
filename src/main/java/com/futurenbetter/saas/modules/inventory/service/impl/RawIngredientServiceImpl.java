package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.filter.RawIngredientFilter;
import com.futurenbetter.saas.modules.inventory.dto.request.RawIngredientRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RawIngredientResponse;
import com.futurenbetter.saas.modules.inventory.entity.RawIngredient;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import com.futurenbetter.saas.modules.inventory.mapper.RawIngredientMapper;
import com.futurenbetter.saas.modules.inventory.repository.IngredientBatchRepository;
import com.futurenbetter.saas.modules.inventory.repository.RawIngredientRepository;
import com.futurenbetter.saas.modules.inventory.service.inter.RawIngredientService;
import com.futurenbetter.saas.modules.inventory.specification.RawIngredientSpec;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RawIngredientServiceImpl implements RawIngredientService {

    private final RawIngredientRepository repository;
    private final IngredientBatchRepository batchRepository;
    private final RawIngredientMapper mapper;


    @Override
    @Transactional
    public RawIngredientResponse create(RawIngredientRequest request) {
        RawIngredient entity = mapper.toEntity(request);
        entity.setShop(SecurityUtils.getCurrentShop());
        entity.setStatus(Status.ACTIVE);

        return toFullResponse(repository.save(entity));
    }


    @Override
    @Transactional
    public RawIngredientResponse update(Long id, RawIngredientRequest request) {
        RawIngredient entity = repository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));

        mapper.updateFromRequest(entity, request);

        return toFullResponse(repository.save(entity));
    }


    @Override
    @Transactional(readOnly = true)
    public Page<RawIngredientResponse> getAll(RawIngredientFilter filter) {
        return repository.findAll(
                RawIngredientSpec.filter(filter, SecurityUtils.getCurrentShopId()),
                filter.getPageable()
        ).map(this::toFullResponse);
    }


    @Override
    @Transactional(readOnly = true)
    public RawIngredientResponse getDetail(Long id) {
        RawIngredient entity = repository.findByIdAndShopId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));
        return toFullResponse(entity);
    }


    private RawIngredientResponse toFullResponse(RawIngredient entity) {
        var res = mapper.toResponse(entity);
        // Tính tổng tồn kho từ Batch (Logic tính toán không thuộc về Mapper)
        Double totalStock = batchRepository.sumQuantityByIngredientIdAndStatus(entity.getId(), Status.ACTIVE);
        res.setTotalStockQuantity(totalStock != null ? totalStock : 0.0);
        return res;
    }
}
