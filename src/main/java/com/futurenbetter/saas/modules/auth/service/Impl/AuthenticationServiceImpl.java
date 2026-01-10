package com.futurenbetter.saas.modules.auth.service.Impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.modules.auth.dto.request.*;
import com.futurenbetter.saas.modules.auth.dto.response.CustomerResponse;
import com.futurenbetter.saas.modules.auth.dto.response.LoginResponse;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminLoginResponse;
import com.futurenbetter.saas.modules.auth.dto.response.SystemAdminRegistrationResponse;
import com.futurenbetter.saas.modules.auth.entity.Customer;
import com.futurenbetter.saas.modules.auth.entity.MembershipRank;
import com.futurenbetter.saas.modules.auth.entity.Role;
import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import com.futurenbetter.saas.modules.auth.enums.ApplyStatus;
import com.futurenbetter.saas.modules.auth.enums.CustomerStatus;
import com.futurenbetter.saas.modules.auth.enums.UserProfileEnum;
import com.futurenbetter.saas.modules.auth.mapper.CustomerMapper;
import com.futurenbetter.saas.modules.auth.mapper.UserProfileMapper;
import com.futurenbetter.saas.modules.auth.repository.CustomerRepository;
import com.futurenbetter.saas.modules.auth.repository.MembershipRankRepository;
import com.futurenbetter.saas.modules.auth.repository.RoleRepository;
import com.futurenbetter.saas.modules.auth.repository.UserProfileRepository;
import com.futurenbetter.saas.modules.auth.service.AuthenticationService;
import com.futurenbetter.saas.modules.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class AuthenticationServiceImpl implements AuthenticationService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final JwtService jwtService;
    private final MembershipRankRepository membershipRankRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileRepository profileRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserProfileMapper userProfileMapper;
    private final RoleRepository roleRepository;

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

        MembershipRank defaultRank = membershipRankRepository.findFirstByShop_IdOrderByRequiredPointsAsc(currentShopId)
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

        if (request.getUsername() == null || request.getPassword() == null) {
            throw new BusinessException("Thiếu thông tin đăng nhập");
        }

        Customer customer = customerRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Tên đăng nhập không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }

        String accessToken = jwtService.generateAccessToken(
                customer.getUsername(),
                currentShopId,
                "SHOP");
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

        String newAccessToken = jwtService.generateAccessToken(
                customer.getUsername(),
                customer.getMembershipRank().getShop().getId(),
                "SHOP");
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
    public void changePassword(Long customerid, ChangePasswordRequest request) {
        Customer customer = customerRepository.findById(customerid)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

        if (!passwordEncoder.matches(request.getOldPassword(), customer.getPassword())) {
            throw new BusinessException("Mật khẩu cũ không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }

        customer.setPassword(passwordEncoder.encode(request.getNewPassword()));
        customer.setUpdatedAt(LocalDateTime.now());

        customerRepository.save(customer);
    }

    @Transactional(readOnly = true)
    @Override
    public SystemAdminLoginResponse loginSystemAdmin(SystemAdminLoginRequest request) {
        UserProfile admin = profileRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BusinessException("Tài khoản admin không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("Mật khẩu không chính xác");
        }

        boolean isSystemUser = admin.getRoles().stream()
                .anyMatch(role -> ApplyStatus.SYSTEM.equals(role.getRole()));

        if (!isSystemUser) {
            throw new BusinessException("Bạn không có quyền truy cập");
        }

        String token = jwtService.generateAccessToken(admin.getUsername(), null, "SYSTEM");
        return SystemAdminLoginResponse.builder().accessToken(token).build();
    }

    @Override
    public SystemAdminRegistrationResponse registerSystemAdmin(SystemAdminRegistrationRequest request) {
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        UserProfile admin = userProfileMapper.toEntity(request);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setStatus(UserProfileEnum.ACTIVE);

        Role adminRole = roleRepository.findByRole(ApplyStatus.SYSTEM)
                .orElseThrow(() -> new BusinessException("Admin chưa được cấu hình"));
        admin.setRoles(Set.of(adminRole));

        UserProfile savedAdmin = userProfileRepository.save(admin);
        return userProfileMapper.toAdminResponse(savedAdmin);

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
