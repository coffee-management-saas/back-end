package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.Role;
import com.futurenbetter.saas.modules.auth.enums.ApplyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRole(ApplyStatus role);
    Optional<Role> findByName(String name);
}
