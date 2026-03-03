package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.Customer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer,Long> {
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    @EntityGraph(attributePaths = {"role", "role.permissions"})
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByRefreshToken(String refreshToken);

    Integer countByShopIdAndCreatedAtBetween(Long shopId, LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT c FROM Customer c
    WHERE c.shop.id = :shopId
    AND SIZE(c.orders) > :minOrders
    AND c.createdAt BETWEEN :from AND :to
""")
    List<Customer> findCustomersWithMinOrders(
            @Param("shopId") Long shopId,
            @Param("minOrders") Integer minOrders,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
//    Optional<Customer> findByUsernameWithRoleAndPermissions(@Param("username") String username);
}
