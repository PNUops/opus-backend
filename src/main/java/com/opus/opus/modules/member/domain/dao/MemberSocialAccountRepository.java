package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.MemberSocialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberSocialAccountRepository extends JpaRepository<MemberSocialAccount, Long> {
}
