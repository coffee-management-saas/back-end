package com.futurenbetter.saas.modules.notification.controller;

import com.futurenbetter.saas.common.dto.response.ApiResponse;
import com.futurenbetter.saas.common.dto.response.PageMeta;
import com.futurenbetter.saas.modules.notification.dto.filter.NotificationFilter;
import com.futurenbetter.saas.modules.notification.dto.response.NotificationResponse;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PutMapping("{id}")
    @PreAuthorize("hasAuthority('notification:update')")
    public ApiResponse<NotificationResponse> update(
            @PathVariable Long id
    ) {
        NotificationResponse response = notificationService.markAsRead(id);

        return ApiResponse.success(
                HttpStatus.OK,
                "Update notification successfully",
                response,
                null
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('notification:read')")
    public ApiResponse<List<NotificationResponse>> getAll(
            @ModelAttribute NotificationFilter filter
    ) {
        Page<NotificationResponse> page = notificationService.getAll(filter);

        PageMeta meta = PageMeta.builder()
                .currentPage(page.getNumber() + 1)
                .size(page.getSize())
                .lastPage(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        return ApiResponse.success(
                HttpStatus.OK,
                "Get notifications successfully",
                page.getContent(),
                meta
        );
    }
}
