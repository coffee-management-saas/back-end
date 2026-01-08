package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MembershipRankRepository extends JpaRepository<MembershipRank, Long> {
    Optional<MembershipRank> findFirstByShop_IdOrderByRequiredPointsAsc(Long shopId);
}
