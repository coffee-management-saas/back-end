package com.futurenbetter.saas.modules.auth.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.auth.dto.filter.MembershipRankFilter;
import com.futurenbetter.saas.modules.auth.dto.request.MembershipRankRequest;
import com.futurenbetter.saas.modules.auth.dto.response.MembershipRankResponse;
import com.futurenbetter.saas.modules.auth.service.MembershipRankService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/membership-ranks")
@RequiredArgsConstructor
public class MembershipRankController {

    private final MembershipRankService rankService;

    @GetMapping
    @PreAuthorize("hasAuthority('membership-rank:read')")
    public ApiResponse<List<MembershipRankResponse>> getRanks(
            MembershipRankFilter filter
    ) {
        Page<MembershipRankResponse> responses = rankService.getRanks(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(responses.getNumber() + 1)
                .size(responses.getSize())
                .lastPage(responses.getTotalPages())
                .totalElements(responses.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy danh sách hạng thành viên thành công",
                responses.getContent(),
                meta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('membership-rank:read-detail')")
    public ApiResponse<MembershipRankResponse> getRankById(
            @PathVariable Long id
    ) {
        MembershipRankResponse response = rankService.getRankById(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Lấy danh sách hạng thành viên thành công",
                response,
                null);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('membership-rank:create')")
    public ApiResponse<MembershipRankResponse> createRank(
            @RequestBody MembershipRankRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.CREATED,
                "Tạo hạng thành viên thành công",
                rankService.createRank(request),
                null);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('membership-rank:update')")
    public ApiResponse<MembershipRankResponse> updateRank(
            @PathVariable Long id,
            @RequestBody MembershipRankRequest request
    ) {
        return ApiResponse.success(
                HttpStatus.OK,
                "Cập nhật hạng thành viên thành công",
                rankService.updateRank(id, request),
                null);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('membership-rank:delete')")
    public ApiResponse<Void> deleteRank(
            @PathVariable Long id
    ) {
        rankService.deleteRank(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Xóa hạng thành viên thành công",
                null,
                null);
    }
}
