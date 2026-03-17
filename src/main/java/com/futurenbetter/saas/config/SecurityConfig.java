package com.futurenbetter.saas.config;

import com.futurenbetter.saas.modules.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsUtils;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

        // Chỉ permit Swagger UI và WebSocket - KHÔNG permit /api/** (quá rộng)
        private static final String[] SWAGGER_ENDPOINTS = {
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api/swagger-ui/**",
                        "/api/docs/**",
                        "/ws/**",
        };

        private final AuthenticationService authenticationService;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
                return http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                                .authorizeHttpRequests(auth -> auth
                                                // Swagger UI và WebSocket
                                                .requestMatchers(SWAGGER_ENDPOINTS).permitAll()
                                                // CORS preflight
                                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                                // Auth endpoints (login, register)
                                                .requestMatchers("/api/auth/**").permitAll()
                                                .requestMatchers("/api/auth/register-customer-system").permitAll()
                                                // System auth
                                                .requestMatchers("/api/system/auth/**").permitAll()
                                                .requestMatchers("/api/system/**").permitAll()
                                                // Public customer info
                                                .requestMatchers("/api/customers/me").permitAll()
                                                // Payment callbacks (MoMo, VNPAY gọi từ bên ngoài, không có token)
                                                .requestMatchers("/api/momo/**").permitAll()
                                                .requestMatchers("/api/subscriptions/momo-callback/**").permitAll()
                                                .requestMatchers("/api/subscriptions/vnpay-return").permitAll()
                                                // Tất cả request còn lại phải authenticate
                                                .anyRequest().authenticated())
                                .formLogin(AbstractHttpConfigurer::disable)
                                .httpBasic(AbstractHttpConfigurer::disable)
                                .userDetailsService(authenticationService)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }
}
