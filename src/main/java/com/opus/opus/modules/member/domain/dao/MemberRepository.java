package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(final String email);

    Optional<Member> findByStudentId(final String studentId);

    Optional<Member> findByStudentIdAndName(final String studentId, final String name);

    Boolean existsByEmail(final String Email);

    Boolean existsByStudentId(final String studentId);
}
