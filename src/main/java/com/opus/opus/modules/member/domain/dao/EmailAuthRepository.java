package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.EmailAuth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailAuthRepository extends JpaRepository<EmailAuth, Long> {
}
