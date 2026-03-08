package com.futurenbetter.saas.modules.auth.service.Impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.dto.filter.MembershipRankFilter;
import com.futurenbetter.saas.modules.auth.dto.request.MembershipRankRequest;
import com.futurenbetter.saas.modules.auth.dto.response.MembershipRankResponse;
import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import com.futurenbetter.saas.modules.auth.entity.Shop;
import com.futurenbetter.saas.modules.auth.enums.MembershipRankStatus;
import com.futurenbetter.saas.modules.auth.mapper.MembershipRankMapper;
import com.futurenbetter.saas.modules.auth.repository.MembershipRankRepository;
import com.futurenbetter.saas.modules.auth.service.MembershipRankService;
import com.futurenbetter.saas.modules.auth.spec.MembershipRankSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MembershipRankServiceImpl implements MembershipRankService {

    private final MembershipRankRepository rankRepository;
    private final MembershipRankMapper rankMapper;

    @Override
    public Page<MembershipRankResponse> getRanks(MembershipRankFilter filter) {
        Long shopId = SecurityUtils.getCurrentShopId();
        Page<MembershipRank> page = rankRepository.findAll(MembershipRankSpecification.filter(shopId, filter), filter.getPageable());
        return page.map(rankMapper::toResponse);
    }

    @Override
    @Transactional
    public MembershipRankResponse createRank(MembershipRankRequest request) {
        Shop currentShop = SecurityUtils.getCurrentShop();

        MembershipRank rank = rankMapper.toEntity(request);
        rank.setShop(currentShop);
        if (rank.getStatus() == null) {
            rank.setStatus(MembershipRankStatus.ACTIVE);
        }

        return rankMapper.toResponse(rankRepository.save(rank));
    }

    @Override
    @Transactional
    public MembershipRankResponse updateRank(Long id, MembershipRankRequest request) {
        MembershipRank rank = rankRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hạng thành viên"));

        // Xác thực hạng này có thuộc về shop hiện tại không
        if (!rank.getShop().getId().equals(SecurityUtils.getCurrentShopId())) {
            throw new BusinessException("Bạn không có quyền chỉnh sửa hạng thành viên này");
        }

        rankMapper.updateRankFromRequest(request, rank);
        rank.setUpdatedAt(LocalDateTime.now());

        return rankMapper.toResponse(rankRepository.save(rank));
    }

    @Override
    @Transactional
    public void deleteRank(Long id) {
        MembershipRank rank = rankRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hạng thành viên"));

        if (!rank.getShop().getId().equals(SecurityUtils.getCurrentShopId())) {
            throw new BusinessException("Bạn không có quyền xóa hạng thành viên này");
        }

        // Tùy nghiệp vụ: Xóa cứng hoặc xóa mềm (chuyển status thành INACTIVE)
        rank.setStatus(MembershipRankStatus.INACTIVE);
        rankRepository.save(rank);
    }

    @Override
    public MembershipRankResponse getRankById(Long id) {
        Long shopId = SecurityUtils.getCurrentShopId();
        MembershipRank rank = rankRepository.findByShopIdAndId(shopId, id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy hạng thành viên"));
        return rankMapper.toResponse(rank);
    }
}
