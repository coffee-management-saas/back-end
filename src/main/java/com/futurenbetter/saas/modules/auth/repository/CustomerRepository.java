package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByRefreshToken(String refreshToken);

    @Query("SELECT c FROM Customer c LEFT JOIN FETCH c.role r LEFT JOIN FETCH r.permissions WHERE c.username = :username")
    Optional<Customer> findByUsernameWithRoleAndPermissions(@Param("username") String username);
}
