package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.IngredientBatch;
import com.futurenbetter.saas.modules.inventory.enums.InventoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientBatchRepository
                extends JpaRepository<IngredientBatch, Long>, JpaSpecificationExecutor<IngredientBatch> {

        // Logic tính tổng tồn kho hiện tại của 1 nguyên liệu
        // Trả về Double (như đã thống nhất đổi Integer -> Double)
        @Query("SELECT SUM(b.currentQuantity) FROM IngredientBatch b " +
                        "WHERE b.rawIngredient.id = :ingredientId AND b.inventoryStatus = :status")
        Double sumQuantityByIngredientIdAndStatus(@Param("ingredientId") Long ingredientId,
                        @Param("status") InventoryStatus inventoryStatus);

        // Lấy danh sách lô còn hàng để trừ kho (FIFO)
        // Sắp xếp: Hết hạn trước -> Trừ trước. Nhập trước -> Trừ trước.
        @Query("SELECT b FROM IngredientBatch b" +
                        " WHERE b.shop.id = :shopId " +
                        "AND b.rawIngredient.id = :ingredientId " +
                        "AND b.currentQuantity > 0 " +
                        "AND b.inventoryStatus = :status " +
                        "ORDER BY b.expiredAt ASC, b.createdAt ASC")
        List<IngredientBatch> findAllAvailableBatchesForDeduction(
                        @Param("shopId") Long shopId,
                        @Param("ingredientId") Long ingredientId,
                        @Param("status") InventoryStatus status);
}