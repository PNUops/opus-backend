package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
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

    boolean existsByTeamIdAndSubmissionItem(final Long teamId, final ContestSubmissionItem submissionItem);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ContestSubmission s SET s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :submissionId")
    void touchUpdatedAt(@Param("submissionId") final Long submissionId);

    void deleteAllBySubmissionItemId(final Long submissionItemId);

    @Query("""
            SELECT s FROM ContestSubmission s
            JOIN FETCH s.submissionItem item
            WHERE s.teamId = :teamId
              AND item.contest.id = :contestId
            ORDER BY s.firstSubmittedAt ASC
            """)
    List<ContestSubmission> findAllByTeamIdAndContestId(@Param("teamId") Long teamId,
                                                        @Param("contestId") Long contestId);
}
