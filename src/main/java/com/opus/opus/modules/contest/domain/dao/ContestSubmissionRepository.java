package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestSubmissionRepository extends JpaRepository<ContestSubmission, Long> {

    // 제출물 종류, 분과별 제출 팀 수와 파일 용량을 DB에서 집계하여 반환
    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.DownloadTargetResult(
                   item.id, item.name, track.id, track.trackName,
                   COUNT(DISTINCT s.teamId), COALESCE(SUM(f.fileSize), 0L))
            FROM ContestSubmission s
            JOIN s.submissionItem item
            JOIN Team team ON team.id = s.teamId
            JOIN ContestTrack track ON track.id = team.trackId
            LEFT JOIN FileDocument fd ON fd.submissionId = s.id
            LEFT JOIN fd.file f
            WHERE item.contest.id = :contestId
              AND (:submissionTypeId IS NULL OR item.id = :submissionTypeId)
              AND (:trackId IS NULL OR track.id = :trackId)
            GROUP BY item.id, item.name, track.id, track.trackName
            ORDER BY item.id, track.id
            """)
    List<DownloadTargetResult> findDownloadTargets(@Param("contestId") Long contestId,
                                                 @Param("submissionTypeId") Long submissionTypeId,
                                                 @Param("trackId") Long trackId);

    // 제출 파일 다운로드 대상 zip 구성을 위한 제출물 단위 (종류·분과·팀명·제출ID) 행 반환
    // 파일 정보는 file 모듈에서 제출ID로 조회한다.
    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.DownloadSubmissionRow(
                   item.id, track.id, team.teamName, s.id)
            FROM ContestSubmission s
            JOIN s.submissionItem item
            JOIN Team team ON team.id = s.teamId
            JOIN ContestTrack track ON track.id = team.trackId
            WHERE item.contest.id = :contestId
            """)
    List<DownloadSubmissionRow> findDownloadSubmissions(@Param("contestId") Long contestId);

    // 멘토 조회용: 팀의 공개(PUBLIC) 제출물을 제출 항목과 함께 조회한다.
    @Query("""
            SELECT s
            FROM ContestSubmission s
            JOIN FETCH s.submissionItem item
            WHERE s.teamId = :teamId
              AND item.contest.id = :contestId
              AND item.visibility = com.opus.opus.modules.contest.domain.SubmissionVisibility.PUBLIC
            ORDER BY item.id
            """)
    List<ContestSubmission> findPublicSubmissionsByTeam(Long contestId, Long teamId);

    // 멘토 조회용: 담당 팀들의 공개(PUBLIC) 제출물 중 해당 멘토가 아직 피드백하지 않은 건수를 팀별로 집계한다.
    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.TeamPendingFeedbackResult(
                   s.teamId, SUM(CASE WHEN f.id IS NULL THEN 1L ELSE 0L END))
            FROM ContestSubmission s
            JOIN s.submissionItem item
            LEFT JOIN ContestSubmissionFeedback f ON f.submission = s AND f.memberId = :memberId
            WHERE item.contest.id = :contestId
              AND item.visibility = com.opus.opus.modules.contest.domain.SubmissionVisibility.PUBLIC
              AND s.teamId IN :teamIds
            GROUP BY s.teamId
            """)
    List<TeamPendingFeedbackResult> findPendingFeedbackCountsByTeams(Long contestId, Long memberId, List<Long> teamIds);

    boolean existsByTeamIdAndSubmissionItem(final Long teamId, final ContestSubmissionItem submissionItem);

    void deleteAllBySubmissionItemId(final Long submissionItemId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ContestSubmission s SET s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :submissionId")
    void touchUpdatedAt(@Param("submissionId") final Long submissionId);

    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.TeamSubmissionStatusResult(
                   i.id, s.id, i.name, i.description, i.endAt, s.firstSubmittedAt)
            FROM ContestSubmissionItem i
            LEFT JOIN ContestSubmission s ON s.submissionItem = i AND s.teamId = :teamId
            WHERE i.contest.id = :contestId
              AND (i.contestTrack IS NULL OR i.contestTrack.id = :trackId)
            ORDER BY i.id
            """)
    List<TeamSubmissionStatusResult> findTeamSubmissionStatuses(final Long contestId, final Long teamId,
                                                                final Long trackId);

    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.UpcomingSubmissionResult(
                   i.id, s.id, i.name, i.endAt, s.updatedAt, s.firstSubmittedAt)
            FROM ContestSubmissionItem i
            LEFT JOIN ContestSubmission s ON s.submissionItem = i AND s.teamId = :teamId
            WHERE i.contest.id = :contestId
              AND (i.contestTrack IS NULL OR i.contestTrack.id = :trackId)
              AND i.endAt > :now
            ORDER BY i.endAt
            """)
    List<UpcomingSubmissionResult> findUpcomingTeamSubmissions(final Long contestId, final Long teamId,
                                                               final Long trackId, final LocalDateTime now);

    @Query(value = """
            SELECT new com.opus.opus.modules.contest.domain.dao.ContestSubmissionStatusResult(
                   s.id, t.id, t.teamName, ct.trackName, i.name, s.firstSubmittedAt, s.updatedAt, i.endAt)
            FROM ContestSubmissionItem i
            CROSS JOIN Team t
            LEFT JOIN ContestSubmission s ON s.submissionItem = i AND s.teamId = t.id
            LEFT JOIN ContestTrack ct ON ct.id = t.trackId
            WHERE i.contest.id = :contestId
              AND t.contestId = :contestId
              AND (i.contestTrack IS NULL OR i.contestTrack.id = t.trackId)
              AND (:submissionItemId IS NULL OR i.id = :submissionItemId)
              AND (:trackId IS NULL OR t.trackId = :trackId)
              AND (:keyword IS NULL OR t.teamName LIKE CONCAT('%', :keyword, '%'))
              AND (:status IS NULL
                   OR (:status = 'SUBMITTED'
                       AND s.id IS NOT NULL AND s.firstSubmittedAt <= i.endAt)
                   OR (:status = 'LATE'
                       AND s.id IS NOT NULL AND s.firstSubmittedAt > i.endAt)
                   OR (:status = 'NOT_SUBMITTED'
                       AND s.id IS NULL AND :now <= i.endAt)
                   OR (:status = 'NOT_SUBMITTED_AFTER_DEADLINE'
                       AND s.id IS NULL AND :now > i.endAt))
            ORDER BY t.id, i.id
            """)
    List<ContestSubmissionStatusResult> findSubmissionStatuses(final Long contestId, final Long submissionItemId,
                                                               final String status, final Long trackId,
                                                               final String keyword, final LocalDateTime now);

    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.ContestSubmissionSummaryResult(
                   COUNT(i),
                   COALESCE(SUM(CASE WHEN s.id IS NOT NULL AND s.firstSubmittedAt <= i.endAt THEN 1 ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN s.id IS NULL THEN 1 ELSE 0 END), 0),
                   COALESCE(SUM(CASE WHEN s.id IS NOT NULL AND s.firstSubmittedAt > i.endAt THEN 1 ELSE 0 END), 0))
            FROM ContestSubmissionItem i
            CROSS JOIN Team t
            LEFT JOIN ContestSubmission s ON s.submissionItem = i AND s.teamId = t.id
            WHERE i.contest.id = :contestId
              AND t.contestId = :contestId
              AND (i.contestTrack IS NULL OR i.contestTrack.id = t.trackId)
              AND (:submissionItemId IS NULL OR i.id = :submissionItemId)
              AND (:trackId IS NULL OR t.trackId = :trackId)
            """)
    ContestSubmissionSummaryResult findSubmissionSummary(final Long contestId, final Long submissionItemId,
                                                         final Long trackId);
}
