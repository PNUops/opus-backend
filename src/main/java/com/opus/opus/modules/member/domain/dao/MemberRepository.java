package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.SocialType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(value = "SELECT * FROM member WHERE id IN :ids", nativeQuery = true)
    List<Member> findAllByIdIncludingDeleted(@Param("ids") List<Long> ids);

    Optional<Member> findByEmail(final String email);

    Optional<Member> findByStudentId(final String studentId);

    Optional<Member> findByStudentIdAndName(final String studentId, final String name);

    Boolean existsByEmail(final String Email);

    Boolean existsByStudentId(final String studentId);

    Optional<Member> findBySocialTypeAndSocialId(final SocialType socialType, final String socialId);
}
