package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.request.RecipeRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RecipeResponse;
import com.futurenbetter.saas.modules.inventory.entity.Recipe;
import com.futurenbetter.saas.modules.inventory.enums.Status;
import com.futurenbetter.saas.modules.inventory.mapper.RecipeMapper;
import com.futurenbetter.saas.modules.inventory.repository.RawIngredientRepository;
import com.futurenbetter.saas.modules.inventory.repository.RecipeRepository;
import com.futurenbetter.saas.modules.inventory.service.inter.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository repository;
    private final RawIngredientRepository ingredientRepository;
    private final RecipeMapper mapper;


    @Override
    @Transactional
    public RecipeResponse create(RecipeRequest request) {
        var ingredient = ingredientRepository.findByIdAndId(request.getIngredientId(), SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại"));

        Recipe entity = mapper.toEntity(request);
        entity.setShop(SecurityUtils.getCurrentShop());
        entity.setRawIngredient(ingredient);
        entity.setStatus(Status.ACTIVE);

        return mapper.toResponse(repository.save(entity));
    }


    @Override
    @Transactional
    public RecipeResponse update(Long id, RecipeRequest request) {
        Recipe entity = repository.findByIdAndId(id, SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Công thức không tồn tại"));

        // 1. Update các field cơ bản (quantity, note...)
        mapper.updateFromRequest(entity, request);

        // 2. Nếu đổi nguyên liệu, cần set lại quan hệ thủ công
        if (request.getIngredientId() != null && !request.getIngredientId().equals(entity.getRawIngredient().getId())) {
            var newIngredient = ingredientRepository.findByIdAndId(request.getIngredientId(), SecurityUtils.getCurrentShopId())
                    .orElseThrow(() -> new BusinessException("Nguyên liệu mới không tồn tại"));
            entity.setRawIngredient(newIngredient);
        }

        return mapper.toResponse(repository.save(entity));
    }


    @Override
    public List<RecipeResponse> getByVariant(Long variantId) {
        return repository.findByVariantIdAndId(variantId, SecurityUtils.getCurrentShopId())
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<RecipeResponse> getByTopping(Long toppingId) {
        return repository.findByToppingIdAndId(toppingId, SecurityUtils.getCurrentShopId())
                .stream().map(mapper::toResponse).collect(Collectors.toList());
    }
}
