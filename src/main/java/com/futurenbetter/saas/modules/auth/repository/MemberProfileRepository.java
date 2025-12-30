package com.futurenbetter.saas.modules.auth.repository;

import com.futurenbetter.saas.modules.auth.entity.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
}
