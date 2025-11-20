package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.SocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
}
