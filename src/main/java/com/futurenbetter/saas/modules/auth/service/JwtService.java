package com.futurenbetter.saas.modules.auth.service;

import com.futurenbetter.saas.modules.auth.entity.Customer;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;
import java.util.function.Function;

public interface JwtService {
    String generateAccessToken(String username, Long shopId);
    String generateRefreshToken(String username);
    String builderToken(Map<String, Object> extraClaims, String subject, long expiration);
    Customer getCustomerByToken(String token);
    String extractUsername(String token);
    boolean isTokenExpired(String token);
}
