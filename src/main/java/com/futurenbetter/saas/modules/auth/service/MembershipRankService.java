package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.dto.filter.MembershipRankFilter;
import com.futurenbetter.saas.modules.auth.dto.request.MembershipRankRequest;
import com.futurenbetter.saas.modules.auth.dto.response.MembershipRankResponse;
import org.springframework.data.domain.Page;

public interface MembershipRankService {
    Page<MembershipRankResponse> getRanks(MembershipRankFilter filter);
    MembershipRankResponse createRank(MembershipRankRequest request);
    MembershipRankResponse updateRank(Long id, MembershipRankRequest request);
    void deleteRank(Long id);
    MembershipRankResponse getRankById(Long id);
}
