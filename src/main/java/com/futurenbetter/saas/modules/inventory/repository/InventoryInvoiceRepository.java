package com.futurenbetter.saas.modules.inventory.repository;

import com.futurenbetter.saas.modules.inventory.entity.InventoryInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryInvoiceRepository extends JpaRepository<InventoryInvoice, Long>, JpaSpecificationExecutor<InventoryInvoice> {
    Optional<InventoryInvoice> findByIdAndShopId(Long id, Long shopId);
}