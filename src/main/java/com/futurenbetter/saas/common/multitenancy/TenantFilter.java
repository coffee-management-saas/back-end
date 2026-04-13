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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();

        if (path.startsWith("/api/system/")) {
            chain.doFilter(request, response);
            return;
        }

        String host = httpRequest.getHeader("X-Forwarded-Host");
        if (host == null || host.isEmpty()) {
            host = httpRequest.getHeader("Host");
        }

        if (host != null) {
            String domain = host.split(":")[0].toLowerCase().trim();

            if (isLocalOrIp(domain)) {
                domain = "abc-shop.com";
                //domain = "futurebetter.online";
            }
            final String finalDomain = domain;

            shopRepository.findByDomain(finalDomain).ifPresentOrElse(shop -> {
                TenantContext.setCurrentShopId(shop.getId());
            }, () -> {
                System.out.println("DEBUG - TenantFilter: No shop found in DB for domain: [" + finalDomain + "]");
            });
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clearCurrentShopId();
        }
    }

    private boolean isLocalOrIp(String domain) {
        return domain.equals("localhost")
                || domain.equals("127.0.0.1")
                || domain.equals("::1")
                || domain.equals("0:0:0:0:0:0:0:1")
                || domain.equals("54.95.117.37")
                || domain.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
    }
}
