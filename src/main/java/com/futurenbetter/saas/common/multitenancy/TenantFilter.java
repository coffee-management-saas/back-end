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
        if (path.startsWith("/api/system/")) {
            chain.doFilter(request, response);
            return;
        }

        String host = httpRequest.getHeader("Host");

        if (host != null) {
            String domain = host.contains(":") ? host.split(":")[0] : host;

            if (domain.equals("localhost") || domain.equals("127.0.0.1")) {
                // fix cứng để test local: map về domain shop có trong DB
                domain = "futurebetter.online";
            }
            // Giữ nguyên mọi domain khác (futurebetter.online, subdomain, v.v.)
            // để tra cứu đúng trong DB

            final String finalDomain = domain.toLowerCase();

            shopRepository.findByDomain(finalDomain).ifPresent(shop -> {
                TenantContext.setCurrentShopId(shop.getId());
            });
        }

        try {
            chain.doFilter(request, response);
        } finally {
            TenantContext.clearCurrentShopId();
        }
    }
}
