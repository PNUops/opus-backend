package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestSubmissionRepository extends JpaRepository<ContestSubmission, Long> {

    boolean existsByTeamIdAndSubmissionItem(final Long teamId, final ContestSubmissionItem submissionItem);

    @Query("SELECT s FROM ContestSubmission s JOIN FETCH s.submissionItem item "
            + "WHERE item.contest.id = :contestId")
    List<ContestSubmission> findAllByContestId(@Param("contestId") final Long contestId);

    // 제출물 종류 × 분과(팀 기준) 조합별 제출 팀 수와 예상 용량(파일 크기 단순 합산)을 DB에서 집계한다.
    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.ArchiveTargetResult(
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
    List<ArchiveTargetResult> findArchiveTargets(@Param("contestId") Long contestId,
                                                 @Param("submissionTypeId") Long submissionTypeId,
                                                 @Param("trackId") Long trackId);

    // 다운로드 대상 zip 구성을 위한 (종류·분과·팀명·파일명·경로) 행. 분과 미배정 팀의 제출물은 제외된다.
    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.ArchiveFileRow(
                   item.id, track.id, team.teamName, f.name, f.filePath)
            FROM ContestSubmission s
            JOIN s.submissionItem item
            JOIN Team team ON team.id = s.teamId
            JOIN ContestTrack track ON track.id = team.trackId
            JOIN FileDocument fd ON fd.submissionId = s.id
            JOIN fd.file f
            WHERE item.contest.id = :contestId
            """)
    List<ArchiveFileRow> findArchiveFileRows(@Param("contestId") Long contestId);
}
