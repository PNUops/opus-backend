package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamLikeRepository extends JpaRepository<TeamLike, Long> {

    Optional<TeamLike> findByMemberIdAndTeam(Long memberId, Team team);

    @Query("""
                SELECT tl
                FROM TeamLike tl
                JOIN tl.team t
                WHERE tl.memberId = :memberId AND t.contestId = :contestId
            """)
    List<TeamLike> findAllByMemberIdAndContestId(Long memberId, Long contestId);

    @Query("""
                SELECT new com.opus.opus.modules.team.domain.dao.MyLikedProjectResult(
                    t.id, t.teamName, t.projectName,
                    c.id, c.contestName
                )
                FROM TeamLike tl
                JOIN tl.team t
                JOIN Contest c ON c.id = t.contestId
                WHERE tl.memberId = :memberId AND tl.isLiked = true
                ORDER BY tl.createdAt DESC
            """)
    List<MyLikedProjectResult> findMyRecentLikedProjects(Long memberId, Pageable pageable);

    @Query("""
                SELECT new com.opus.opus.modules.team.domain.dao.MyLikedProjectResult(
                    t.id, t.teamName, t.projectName,
                    c.id, c.contestName
                )
                FROM TeamLike tl
                JOIN tl.team t
                JOIN Contest c ON c.id = t.contestId
                WHERE tl.memberId = :memberId AND tl.isLiked = true
                AND (:startDate IS NULL OR tl.createdAt >= :startDate)
                AND (:endDate IS NULL OR tl.createdAt < :endDate)
                AND (:categoryId IS NULL OR c.categoryId = :categoryId)
                AND (:contestId IS NULL OR c.id = :contestId)
            """)
    Page<MyLikedProjectResult> findMyLikedProjects(Long memberId, LocalDateTime startDate, LocalDateTime endDate, Long categoryId, Long contestId, Pageable pageable);
}
