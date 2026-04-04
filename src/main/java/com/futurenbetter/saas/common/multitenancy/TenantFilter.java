package com.futurenbetter.saas.common.multitenancy;

import com.futurenbetter.saas.modules.auth.repository.ShopRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter implements Filter {
    private final ShopRepository shopRepository;

    // lấy domain từ httpRequest để xác định shopId
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        // 1. Bỏ qua các path hệ thống
        if (path.startsWith("/api/system/")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Lấy host (Ưu tiên X-Forwarded-Host từ Proxy/Nginx)
        String host = httpRequest.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = httpRequest.getHeader("Host");
        }

        if (host != null) {
            // 3. Cắt bỏ port nếu có (ví dụ: futurebetter.online:8080 -> futurebetter.online)
            String domain = host.split(":")[0].toLowerCase().trim();

            // 4. Mapping các domain đặc biệt về domain mặc định trong DB
            if (isLocalOrIp(domain)) {
                domain = "futurebetter.online";
            }

            final String finalDomain = domain;

            // 5. Truy vấn DB
            shopRepository.findByDomain(finalDomain).ifPresentOrElse(shop -> {
                System.out.println("DEBUG - TenantFilter: Found shop ID " + shop.getId() + " for [" + finalDomain + "]");
                TenantContext.setCurrentShopId(shop.getId());
            }, () -> {
                // In ra domain để biết tại sao nó không khớp với DB
                System.out.println("DEBUG - TenantFilter: No shop found in DB for domain: [" + finalDomain + "]");
            });
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clearCurrentShopId();
        }
    }

    // Tách hàm check IP/Local cho sạch code
    private boolean isLocalOrIp(String domain) {
        return domain.equals("localhost")
                || domain.equals("127.0.0.1")
                || domain.equals("::1")
                || domain.equals("0:0:0:0:0:0:0:1")
                || domain.equals("54.95.117.37")
                || domain.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }
}
