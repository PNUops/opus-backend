package com.opus.opus.modules.member.domain.dao;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.SocialType;
import java.util.List;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    // 관리자 회원 목록은 탈퇴 회원도 포함해야 하므로 @SQLRestriction을 우회하는 native 쿼리를 사용한다.
    // keyword는 서비스에서 LIKE 와일드카드를 \로 이스케이프해 전달하며, MySQL LIKE의 기본 이스케이프 문자가 \이다.
    String ADMIN_MEMBER_FILTER = """
            FROM member m
            WHERE m.is_fake = false
              AND (:isDeleted IS NULL OR m.is_deleted = :isDeleted)
              AND (:role IS NULL OR EXISTS (
                    SELECT 1 FROM member_roles r WHERE r.member_id = m.id AND r.role = :role))
              AND (:keyword IS NULL
                    OR LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(m.student_id) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """;

    @Query(value = "SELECT * FROM member WHERE id IN :ids", nativeQuery = true)
    List<Member> findAllByIdIncludingDeleted(@Param("ids") List<Long> ids);

    @Query(value = "SELECT m.* " + ADMIN_MEMBER_FILTER + " ORDER BY m.created_at DESC, m.id DESC",
            countQuery = "SELECT COUNT(*) " + ADMIN_MEMBER_FILTER,
            nativeQuery = true)
    Page<Member> findAdminMembers(@Param("keyword") String keyword,
                                  @Param("role") String role,
                                  @Param("isDeleted") Boolean isDeleted,
                                  Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM member WHERE is_fake = false AND is_deleted = :isDeleted", nativeQuery = true)
    long countNonFakeMembersByIsDeleted(@Param("isDeleted") boolean isDeleted);

    @Query("""
            SELECT m FROM Member m
            WHERE LOWER(m.email) LIKE LOWER(CONCAT(:keyword, '%')) ESCAPE '\\'
              AND (:roleType IS NULL OR :roleType MEMBER OF m.roles)
            ORDER BY m.email ASC
            """)
    List<Member> searchByEmailPrefix(@Param("keyword") String keyword,
                                     @Param("roleType") MemberRoleType roleType,
                                     Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :id")
    Optional<Member> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE :role MEMBER OF m.roles AND m.isFake = false ORDER BY m.id")
    List<Member> findAllByRoleForUpdate(@Param("role") MemberRoleType role);

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
