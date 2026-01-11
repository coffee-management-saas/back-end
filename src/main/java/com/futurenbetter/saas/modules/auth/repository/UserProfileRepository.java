package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
//    @Query("SELECT u FROM UserProfile u LEFT JOIN FETCH u.roles WHERE u.username = :username")
//    Optional<UserProfile> findByUsername(@Param("username") String username);
    Optional<UserProfile> findByUsername(String username);
    @Query("SELECT u FROM UserProfile u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<UserProfile> findByUsernameWithRoles(@Param("username") String username);
    boolean existsByUsername(String username);
}
