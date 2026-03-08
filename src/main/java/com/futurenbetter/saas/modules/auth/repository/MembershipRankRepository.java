package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MembershipRankRepository extends JpaRepository<MembershipRank, Long>, JpaSpecificationExecutor<MembershipRank> {
    Optional<MembershipRank> findFirstByShop_IdOrderByRequiredPointsAsc(Long shopId);
    List<MembershipRank> findByShopIdOrderByRequiredPointsDesc(Long shopId);

    Optional<MembershipRank> findByShopIdAndId(Long shopId, Long id);
}
