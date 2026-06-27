package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestSubmissionRepository extends JpaRepository<ContestSubmission, Long> {

    // 제출물 종류와 분과별 제출 팀 수와 파일 용량을 DB에서 집계하여 반환
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

    // 다운로드 대상 zip 구성을 위한 (종류·분과·팀명·파일명·경로) 행 반환
    @Query("""
            SELECT new com.opus.opus.modules.contest.domain.dao.DownloadFileRow(
                   item.id, track.id, team.teamName, f.name, f.filePath)
            FROM ContestSubmission s
            JOIN s.submissionItem item
            JOIN Team team ON team.id = s.teamId
            JOIN ContestTrack track ON track.id = team.trackId
            JOIN FileDocument fd ON fd.submissionId = s.id
            JOIN fd.file f
            WHERE item.contest.id = :contestId
            """)
    List<DownloadFileRow> findDownloadFileRows(@Param("contestId") Long contestId);
}
