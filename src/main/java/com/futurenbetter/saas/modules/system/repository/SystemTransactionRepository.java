package com.futurenbetter.saas.modules.system.repository;

import com.futurenbetter.saas.modules.system.entity.SystemTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SystemTransactionRepository extends JpaRepository<SystemTransaction, Long>, JpaSpecificationExecutor<SystemTransaction> {
}
