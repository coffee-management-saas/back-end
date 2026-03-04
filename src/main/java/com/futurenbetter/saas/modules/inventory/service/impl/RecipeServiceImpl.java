package com.futurenbetter.saas.modules.inventory.service.impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.inventory.dto.request.RecipeRequest;
import com.futurenbetter.saas.modules.inventory.dto.response.RecipeResponse;
import com.futurenbetter.saas.modules.inventory.entity.Recipe;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import com.futurenbetter.saas.modules.inventory.mapper.RecipeMapper;
import com.futurenbetter.saas.modules.inventory.repository.RawIngredientRepository;
import com.futurenbetter.saas.modules.inventory.repository.RecipeRepository;
import com.futurenbetter.saas.modules.inventory.service.inter.RecipeService;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final RawIngredientRepository ingredientRepository;
    private final RecipeMapper recipeMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public List<RecipeResponse> create(RecipeRequest request) {
        Long shopId = SecurityUtils.getCurrentShopId();
        var shop = SecurityUtils.getCurrentShop();
        Long shopAdminId = SecurityUtils.getCurrentUserId();

        if (request.getVariantId() != null) {
            recipeRepository.deleteByVariantIdAndShopId(request.getVariantId(), shopId);
        } else if (request.getToppingId() != null) {
            recipeRepository.deleteByToppingIdAndShopId(request.getToppingId(), shopId);
        } else {
            throw new BusinessException("Phải chỉ định variantId hoặc toppingId");
        }

        List<Recipe> entities = request.getItems().stream().map(item -> {
            var ingredient = ingredientRepository.findByIdAndShopId(item.getIngredientId(), shopId)
                    .orElseThrow(() -> new BusinessException("Nguyên liệu không tồn tại: " + item.getIngredientId()));

            Recipe recipe = new Recipe();
            recipe.setShop(shop);
            recipe.setRawIngredient(ingredient);
            recipe.setVariantId(request.getVariantId());
            recipe.setToppingId(request.getToppingId());
            recipe.setQuantityRequired(item.getQuantityRequired());
            recipe.setNote(item.getNote());
            recipe.setInventoryStatus(InventoryStatus.ACTIVE);
            return recipe;
        }).collect(Collectors.toList());

        List<Recipe> results = recipeRepository.saveAll(entities);

        Notification noti = Notification.builder()
                .title("Tạo công thức thành công")
                .message("Tạo " + results.stream().count() + " công thức thành công")
                .type(NotificationType.INVENTORY)
                .recipientType("SHOP")
                .recipientId(shopAdminId)
                .referenceLink("api/inventory/recipes/save-batch" )
                .shop(shop)
                .build();

        notificationService.sendToUser(noti);

        return results.stream()
                .map(recipeMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeResponse> getByVariant(Long variantId) {
        return recipeRepository.findByVariantIdAndShopId(variantId, SecurityUtils.getCurrentShopId())
                .stream().map(recipeMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecipeResponse> getByTopping(Long toppingId) {
        return recipeRepository.findByToppingIdAndShopId(toppingId, SecurityUtils.getCurrentShopId())
                .stream().map(recipeMapper::toResponse).collect(Collectors.toList());
    }
}
