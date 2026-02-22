package com.futurenbetter.saas.config;

import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import com.futurenbetter.saas.modules.auth.repository.CustomerRepository;
import com.futurenbetter.saas.modules.auth.repository.UserProfileRepository;
import com.futurenbetter.saas.modules.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Lấy toàn bộ claims để đọc user_type
                Claims claims = jwtService.extractAllClaims(token);
                String userType = claims.get("user_type", String.class);
                String authenRole = claims.get("role", String.class);

                List<GrantedAuthority> authorities = new ArrayList<>();
                Object principal = null;

                if ("CUSTOMER".equals(authenRole)) {
                    Customer customer = customerRepository.findByUsernameWithRoleAndPermissions(username).orElse(null);
                    if (customer != null) {
                        principal = customer;

                        if (customer.getRole() != null) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + customer.getRole().getName().toUpperCase()));
                            if (customer.getRole().getPermissions() != null) {
                                authorities.addAll(customer.getRole().getPermissions().stream()
                                        .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName()))
                                        .collect(Collectors.toList()));
                            }
                        } else {
                            authorities.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                        }
                    }
                }
                else { // apply cho shop, employee, và system
                    UserProfile userProfile = userProfileRepository.findByUsernameWithRoles(username).orElse(null);
                    if (userProfile != null) {
                        principal = userProfile;

                        if (userProfile.getRoles() != null) {
                            userProfile.getRoles().forEach(role -> {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName().toUpperCase()));

                                if (role.getPermissions() != null) {
                                    authorities.addAll(role.getPermissions().stream()
                                            .map(permission -> new SimpleGrantedAuthority(permission.getPermissionName()))
                                            .collect(Collectors.toList()));
                                }
                            });
                        }
                    }
                }

                if (principal != null) {
                    List<GrantedAuthority> uniqueAuthorities = authorities.stream()
                            .distinct()
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            uniqueAuthorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.info("User authenticated: {}, Type: {}, Authorities: {}", username, userType, uniqueAuthorities);
                } else {
                    log.warn("User user not found: {}", username);
                }
            }
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            log.error("Authentication failed for token: {}", token, e);
        }


        filterChain.doFilter(request, response);
    }
}
