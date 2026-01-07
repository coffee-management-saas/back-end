package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    Optional<Shop> findByDomain(String domain);
}
