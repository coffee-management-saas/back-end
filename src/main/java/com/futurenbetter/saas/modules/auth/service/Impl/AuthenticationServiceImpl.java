package com.futurenbetter.saas.modules.auth.service.Impl;

import com.futurenbetter.saas.common.exception.BusinessException;
import com.futurenbetter.saas.common.multitenancy.TenantContext;
import com.futurenbetter.saas.common.utils.PasswordUtils;
import com.futurenbetter.saas.common.utils.SecurityUtils;
import com.futurenbetter.saas.modules.auth.dto.request.*;
import com.futurenbetter.saas.modules.auth.dto.response.*;
import com.futurenbetter.saas.modules.auth.entity.*;
import com.futurenbetter.saas.modules.auth.enums.ApplyStatus;
import com.futurenbetter.saas.modules.auth.enums.CustomerStatus;
import com.futurenbetter.saas.modules.auth.enums.UserProfileEnum;
import com.futurenbetter.saas.modules.auth.mapper.CustomerMapper;
import com.futurenbetter.saas.modules.auth.mapper.UserProfileMapper;
import com.futurenbetter.saas.modules.auth.repository.*;
import com.futurenbetter.saas.modules.auth.service.AuthenticationService;
import com.futurenbetter.saas.modules.auth.service.JwtService;
import com.futurenbetter.saas.modules.employee.dto.request.EmployeeRequest;
import com.futurenbetter.saas.modules.employee.dto.response.EmployeeResponse;
import com.futurenbetter.saas.modules.employee.entity.Employee;
import com.futurenbetter.saas.modules.employee.service.inter.EmployeeService;
import com.futurenbetter.saas.modules.notification.entity.Notification;
import com.futurenbetter.saas.modules.notification.enums.NotificationType;
import com.futurenbetter.saas.modules.notification.service.inter.NotificationService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
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
    private final ShopRepository shopRepository;
    private final EmployeeService employeeService;
    private final NotificationService notificationService;

    @Override
    @Transactional
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

        Shop shop = shopRepository.findById(currentShopId)
                .orElseThrow(() -> new BusinessException("Cửa hàng không hợp lệ"));



        Customer customer = customerMapper.toEntity(request);
        customer.setShop(shop);
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        customer.setAddress(request.getAddress());
        customer.setPassword(encodedPassword);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setRole(roleRepository.findByRole(ApplyStatus.CUSTOMER)
                .orElseThrow(() -> new BusinessException("Role CUSTOMER chưa được cấu hình")));
        customer = customerRepository.save(customer);

        MembershipRank defaultRank = membershipRankRepository.findFirstByShop_IdOrderByRequiredPointsAsc(currentShopId)
                .orElseThrow(() -> new BusinessException("Lỗi cấu hình hạng thành viên!"));

        customer.setMembershipRank(defaultRank);
        customer = customerRepository.save(customer);

        return customerMapper.toResponse(customer);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new BusinessException("Thiếu thông tin đăng nhập");
        }

        String username = request.getUsername();
        String password = request.getPassword();
        Long shopId = TenantContext.getCurrentShopId();// Giữ nguyên null nếu FE không truyền

        // customer
        var customerOpt = customerRepository.findByUsername(username);
        if (customerOpt.isPresent()) {
            if (shopId == null || shopId == 0) {
                throw new BusinessException("Vui lòng chọn cửa hàng để đăng nhập");
            }

            Customer customer = customerOpt.get();
            // Giả định hàm validateLogin của bạn có check matching giữa customer.getShop() và shopId
            validateLogin(password, customer.getPassword(), customer.getShop(), shopId);

            Notification noti = Notification.builder()
                    .title("Chúc mừng customer login thành công")
                    .message("abc")
                    .type(NotificationType.AUTHENTICATION)
                    .recipientType("CUSTOMER")
                    .recipientId(customer.getId())
                    .referenceLink("/customer/auth/" + customer.getId())
                    .build();

            notificationService.sendToUser(noti);

            return generateLoginResponse(customer.getUsername(), shopId, "CUSTOMER", customer);
        }

        // check SHOP or EMPLOYEE
        var profileOpt = profileRepository.findByUsernameWithRoles(username);
        if (profileOpt.isPresent()) {
            UserProfile userProfile = profileOpt.get();

            boolean isShopUser = userProfile.getRoles().stream()
                    .anyMatch(role -> ApplyStatus.SHOP.equals(role.getRole()));

            boolean isEmployeeUser = userProfile.getRoles().stream()
                    .anyMatch(role -> ApplyStatus.EMPLOYEE.equals(role.getRole()));

            if (isShopUser) {
                if (shopId == null || shopId == 0) {
                    throw new BusinessException("Vui lòng chọn cửa hàng để đăng nhập");
                }

                validateLogin(password, userProfile.getPassword(), userProfile.getShop(), shopId);

                Notification noti = Notification.builder()
                        .title("Chúc mừng shop_user login thành công")
                        .message("abc")
                        .type(NotificationType.AUTHENTICATION)
                        .recipientType("SHOP")
                        .recipientId(userProfile.getUserProfileId())
                        .referenceLink("/shop/auth/" + userProfile.getUserProfileId())
                        .build();

                notificationService.sendToUser(noti);
                return generateLoginResponse(userProfile.getUsername(), shopId, "SHOP", userProfile);
            }

            if (isEmployeeUser) {
                if (shopId == null || shopId == 0) {
                    throw new BusinessException("Vui lòng chọn cửa hàng để đăng nhập");
                }

                validateLogin(password, userProfile.getPassword(), userProfile.getShop(), shopId);

                Notification noti = Notification.builder()
                        .title("Chúc mừng shop_user login thành công")
                        .message("abc")
                        .type(NotificationType.AUTHENTICATION)
                        .recipientType("EMPLOYEE")
                        .recipientId(userProfile.getUserProfileId())
                        .referenceLink("/shop/auth/" + userProfile.getUserProfileId())
                        .build();

                notificationService.sendToUser(noti);
                return generateLoginResponse(userProfile.getUsername(), shopId, "EMPLOYEE", userProfile);
            }

            // Nếu UserProfile không có role hợp lệ
            throw new BusinessException("Tài khoản này chưa được cấp quyền truy cập hợp lệ");
        }

        // Nếu không tìm thấy trong cả 2 bảng
        throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
    }

    private void validateLogin(String rawPassword, String encodedPassword, Shop userShop, Long currentShopId) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        if (userShop == null || !userShop.getId().equals(currentShopId)) {
            throw new BusinessException("Tài khoản không thuộc cửa hàng này");
        }
    }

    private LoginResponse generateLoginResponse(String username, Long shopId, String role, Object entity) {
        String accessToken = jwtService.generateAccessToken(username, shopId, role);
        String refreshToken = jwtService.generateRefreshToken(username);

        if (entity instanceof Customer customer) {
            customer.setRefreshToken(refreshToken);
            customer.setUpdatedAt(LocalDateTime.now());
            customerRepository.save(customer);
        } else if (entity instanceof UserProfile shop) {
            shop.setRefreshToken(refreshToken);
            shop.setUpdatedAt(LocalDateTime.now());
            userProfileRepository.save(shop);
        }

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .shopId(shopId)
                .role(role)
                .build();
    }

    @Override
    // update lại lấy role từ claims
    public LoginResponse refreshToken(String refreshToken) {
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new BusinessException("Refresh token expired");
        }

        String username = jwtService.extractUsername(refreshToken);

        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Người dùng không tồn tại"));

        Claims claims = jwtService.extractAllClaims(refreshToken);
        String role = claims.get("role", String.class);

        String newAccessToken = jwtService.generateAccessToken(
                customer.getUsername(),
                customer.getMembershipRank().getShop().getId(),
                role);
        String newRefreshToken = jwtService.generateRefreshToken(customer.getUsername());

        customer.setRefreshToken(newRefreshToken);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public void logout(String refreshToken) {
        Customer customer = customerRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Token không hợp lệ hoặc đã hết hạn"));

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new BusinessException("Refresh token expired");
        }

        customer.setRefreshToken(null);
        customer.setUpdatedAt(LocalDateTime.now());
        customerRepository.save(customer);
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

    @Override
    public SystemAdminLoginResponse loginSystemAdmin(SystemAdminLoginRequest request) {
        UserProfile admin = profileRepository.findByUsernameWithRoles(request.getUsername())
                .orElseThrow(() -> new BusinessException("Tài khoản admin không tồn tại"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("Mật khẩu không chính xác");
        }

        boolean isSystemUser = admin.getRoles().stream()
                .anyMatch(role -> ApplyStatus.SYSTEM.equals(role.getRole()));

        if (!isSystemUser) {
            throw new BusinessException("Bạn không có quyền truy cập");
        }

        String acccessToken = jwtService.generateAccessToken(admin.getUsername(), null, "SYSTEM");
        String refreshToken = jwtService.generateRefreshToken(admin.getUsername());

        admin.setRefreshToken(refreshToken);
        admin.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(admin);

        return SystemAdminLoginResponse.builder()
                .accessToken(acccessToken)
                .refreshToken(refreshToken)
                .role("SYSTEM")
                .build();
    }

    @Override
    @Transactional
    public SystemAdminRegistrationResponse registerSystemAdmin(SystemAdminRegistrationRequest request) {
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        UserProfile admin = userProfileMapper.toEntity(request);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setStatus(UserProfileEnum.ACTIVE);

        admin.setAddress(request.getAddress());
        admin.setDob(request.getDob());

        Role adminRole = roleRepository.findByRole(ApplyStatus.SYSTEM)
                .orElseThrow(() -> new BusinessException("Admin chưa được cấu hình"));

        Set<Role> roles = new HashSet<>();
        roles.add(adminRole);
        admin.setRoles(roles);

        UserProfile savedAdmin = userProfileRepository.save(admin);
        return userProfileMapper.toAdminResponse(savedAdmin);

    }

    @Override
    @Transactional
    public SystemAdminRegistrationResponse registerShopAdmin(ShopAdminRegistrationRequest request) {
        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        Shop shop = shopRepository.findById(TenantContext.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        UserProfile admin = userProfileMapper.toEntity(request);
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setStatus(UserProfileEnum.ACTIVE);
        admin.setShop(shop);

        Role shopRole = roleRepository.findByRole(ApplyStatus.SHOP) // set tạm là manager, có thể tạo thêm shop-admin sau
                .orElseThrow(() -> new BusinessException("Role quản trị quán chưa được cấu hình"));
        admin.setRoles(Set.of(shopRole));

        UserProfile savedAdmin = userProfileRepository.save(admin);
        return userProfileMapper.toAdminResponse(savedAdmin);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return (UserDetails) customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }


    @Override
    @Transactional
    public ShopEmployeeRegistrationResponse createShopEmployee(ShopEmployeeRegistrationRequest request) {

        if (userProfileRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }

        Shop shop = shopRepository.findById(SecurityUtils.getCurrentShopId())
                .orElseThrow(() -> new BusinessException("Cửa hàng không tồn tại"));

        // tạo pass ngẫu nhiên và trả ve để employee tự đổi pass lần sau
        String password = PasswordUtils.generateRandomPassword();

        UserProfile employeeProfile = userProfileMapper.toEntity(request);
        employeeProfile.setPassword(passwordEncoder.encode(password));
        employeeProfile.setStatus(UserProfileEnum.ACTIVE);
        employeeProfile.setShop(shop);

        // set tạm role, sẽ phân quyền động sau
        Role shopRole = roleRepository.findByRole(ApplyStatus.EMPLOYEE)
                .orElseThrow(() -> new BusinessException("Role nhân viên quán chưa được cấu hình"));
        employeeProfile.setRoles(Set.of(shopRole));
        // tạo profile trc
        UserProfile savedEmployee = userProfileRepository.save(employeeProfile);
        // sau đó dùng profile để tạo employee
        EmployeeResponse employee = employeeService.createEmployee(EmployeeRequest.builder()
                .employeeType(request.getEmployeeType())
                .hourlyWage(request.getHourlyWage())
                .userProfileId(savedEmployee.getUserProfileId())
                .weeklyHourLimit(request.getWeeklyHourLimit())
                .build()
        );
        // trả về password cho shop-admin lưu lại gửi cho nhân viên
        return userProfileMapper.toEmployeeResponse(savedEmployee, employee, password);
    }

    @Override
    public LoginResponse loginShopAdmin(LoginRequest loginRequest) {
        UserProfile admin = profileRepository.findByUsernameWithRoles(loginRequest.getUsername())
                .orElseThrow(() -> new BusinessException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
            throw new BusinessException("Mật khẩu không chính xác");
        }

        boolean isShopAdmin = admin.getRoles().stream()
                .anyMatch(role -> "SHOP_ADMIN".equals(role.getName()));

        if (!isShopAdmin) {
            throw new BusinessException("Tài khoản không có quyền truy cập quản trị quán");
        }

        if (admin.getShop() == null) {
            throw new BusinessException("Tài khoản chưa được gán vào cửa hàng cụ thể");
        }

        Long shopId = admin.getShop().getId();

        String accessToken = jwtService.generateAccessToken(
                admin.getUsername(),
                shopId,
                "SHOP");

        String refreshToken = jwtService.generateRefreshToken(admin.getUsername());

        admin.setRefreshToken(refreshToken);
        admin.setUpdatedAt(LocalDateTime.now());
        userProfileRepository.save(admin);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .shopId(shopId)
                .build();
    }

    @Override
    public void employeeChangePassword(Long userProfileId, ChangePasswordRequest request) {
        UserProfile employee = userProfileRepository.findById(userProfileId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));

        if (!passwordEncoder.matches(request.getOldPassword(), employee.getPassword())) {
            throw new BusinessException("Mật khẩu cũ không chính xác");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("Mật khẩu xác nhận không khớp");
        }

        employee.setPassword(passwordEncoder.encode(request.getNewPassword()));
        employee.setUpdatedAt(LocalDateTime.now());

        userProfileRepository.save(employee);
    }

}
