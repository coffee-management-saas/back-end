package com.futurenbetter.saas.modules.auth.service.Impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.auth.dto.request.CustomerRegistrationRequest;
import com.futurenbetter.saas.modules.auth.dto.request.LoginRequest;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.dto.response.LoginResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import com.futurenbetter.saas.modules.auth.enums.CustomerStatus;
import com.futurenbetter.saas.modules.auth.mapper.CustomerMapper;
import com.futurenbetter.saas.modules.auth.repository.CustomerRepository;
import com.futurenbetter.saas.modules.auth.repository.MembershipRankRepository;
import com.futurenbetter.saas.modules.auth.service.AuthenticationService;
import com.futurenbetter.saas.modules.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final JwtService jwtService;
    private final MembershipRankRepository membershipRankRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public CustomerResponse register(CustomerRegistrationRequest request) {
        Long currentShopId = TenantContext.getCurrentShopId();

        if (currentShopId == null) {
            throw new BusinessException("Cửa hàng không tồn tại");
        }

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email đã tồn tại");
        }

        if (customerRepository.existsByUsername(request.getUsername())) { //
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        Customer customer = customerMapper.toEntity(request);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        customer.setPassword(encodedPassword);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer = customerRepository.save(customer);

        MembershipRank defaultRank = membershipRankRepository.findFirstByShop_ShopIdOrderByRequiredPointsAsc(currentShopId)
                .orElseThrow(() -> new BusinessException("Lỗi cấu hình hạng thành viên!"));

        customer.setMembershipRank(defaultRank);
        customer = customerRepository.save(customer);

        return customerMapper.toResponse(customer);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Long currentShopId = TenantContext.getCurrentShopId();

        if (currentShopId == null) {
            throw new BusinessException("Cửa hàng không hợp lệ");
        }

        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Tên đăng nhập hoặc mật khẩu không đúng"));


        String accessToken = jwtService.generateAccessToken(customer.getUsername(), currentShopId);
        String refreshToken = jwtService.generateRefreshToken(customer.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public LoginResponse refreshToken(String token) {
        if (jwtService.isTokenExpired(token)) {
            throw new BusinessException("Refresh token expired");
        }

        String username = jwtService.extractUsername(token);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại"));

        String newAccessToken = jwtService.generateAccessToken(customer.getUsername(), customer.getMembershipRank().getShop().getShopId());
        String newRefreshToken = jwtService.generateRefreshToken(customer.getUsername());

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String token) {
        if (jwtService.isTokenExpired(token)) {
            throw new BusinessException("Refresh token expired");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
