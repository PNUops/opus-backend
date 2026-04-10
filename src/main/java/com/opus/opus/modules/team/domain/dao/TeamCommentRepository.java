package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamComment;
import com.opus.opus.modules.team.domain.dao.projection.MyCommentProjection;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamCommentRepository extends JpaRepository<TeamComment, Long> {
    List<TeamComment> findAllByTeamIdOrderByIdDesc(Long id);

    @Query("""
                SELECT
                    tc.id AS commentId,
                    tc.description AS content,
                    tc.createdAt AS createdAt,
                    m.name AS memberName,
                    c.id AS contestId,
                    c.contestName AS contestName,
                    cc.categoryName AS categoryName,
                    ct.trackName AS trackName,
                    t.id AS teamId,
                    t.teamName AS teamName,
                    t.projectName AS projectName,
                    SUBSTRING(t.overview, 1, 100) AS overview
                FROM TeamComment tc
                JOIN tc.team t
                JOIN Member m ON m.id = tc.memberId
                JOIN Contest c ON c.id = t.contestId
                JOIN ContestCategory cc ON cc.id = c.categoryId
                LEFT JOIN ContestTrack ct ON ct.id = t.trackId
                WHERE tc.memberId = :memberId
                AND (:startDate IS NULL OR tc.createdAt >= :startDate)
                AND (:endDate IS NULL OR tc.createdAt < :endDate)
            """)
    Page<MyCommentProjection> findMyComments(Long memberId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
