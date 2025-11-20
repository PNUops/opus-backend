package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
