package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.SocialType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query(value = "SELECT * FROM member WHERE id IN :ids", nativeQuery = true)
    List<Member> findAllByIdIncludingDeleted(@Param("ids") List<Long> ids);

    @Query("""
            SELECT DISTINCT m FROM Member m
            LEFT JOIN m.roles r
            WHERE LOWER(m.email) LIKE LOWER(CONCAT(:keyword, '%')) ESCAPE '\\'
              AND (:roleType IS NULL OR r = :roleType)
            ORDER BY m.email ASC
            """)
    List<Member> searchByEmailPrefix(@Param("keyword") String keyword,
                                     @Param("roleType") MemberRoleType roleType,
                                     Pageable pageable);

    Optional<Member> findByEmail(final String email);

    Optional<Member> findByStudentId(final String studentId);

    Optional<Member> findByStudentIdAndName(final String studentId, final String name);

    Boolean existsByEmail(final String Email);

    Boolean existsByStudentId(final String studentId);

    Optional<Member> findBySocialTypeAndSocialId(final SocialType socialType, final String socialId);

    List<Member> findAllByEmailIn(final List<String> emails);

    List<Member> findAllByStudentIdIn(final List<String> studentIds);

    long countByIsFakeFalse();

    @Query(value = "SELECT COUNT(*) FROM member", nativeQuery = true)
    long countAllIncludingDeletedAndFake();
}
