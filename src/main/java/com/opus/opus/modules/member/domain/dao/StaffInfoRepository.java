package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.StaffInfo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffInfoRepository extends JpaRepository<StaffInfo, Long> {

    Optional<StaffInfo> findByEmailAndName(final String email, final String name);
}
