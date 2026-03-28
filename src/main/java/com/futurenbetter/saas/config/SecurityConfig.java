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

    private static final String[] strings = {
            "/api/**",
            "/v3/api-docs/**",
            "/api/swagger-ui/**",
            "/api/swagger-ui.html",
            "/api/docs/**",
            "/swagger-ui/**",
            "/ws/**",
            "/payment/**",
    };
    private final AuthenticationService authenticationService;
        private static final String[] PUBLIC_ENDPOINTS = strings;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http,
                        JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
                return http
                                .cors(Customizer.withDefaults())
                                .csrf(AbstractHttpConfigurer::disable)
                                // .authorizeHttpRequests(auth -> auth
                                // .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
                                // .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                // .anyRequest().authenticated()
                                // )
                                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/auth/**", "/api/system/auth/**").permitAll()
                                                .requestMatchers("/api/auth/register-customer-system").permitAll()
                                                .requestMatchers("/api/customers/me").permitAll()
                                                .requestMatchers("/api/momo/**").permitAll()
                                                .requestMatchers("/api/subscriptions/momo-callback/**").permitAll()
                                                .requestMatchers("/api/subscriptions/vnpay-return").permitAll()
                                                .requestMatchers("/api/system/**").permitAll()//.hasAuthority("SYSTEM")
                                                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                                .requestMatchers(CorsUtils::isPreFlightRequest).permitAll()
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
