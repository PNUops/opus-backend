package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestSubmissionItemRepository extends JpaRepository<ContestSubmissionItem, Long> {

    List<ContestSubmissionItem> findAllByContestIdOrderByUpdatedAtDesc(final Long contestId);

    @Query("SELECT i FROM ContestSubmissionItem i WHERE i.contest.id = :contestId AND i.endAt > :now AND (i.contestTrack IS NULL OR i.contestTrack.id = :trackId)")
    List<ContestSubmissionItem> findFutureItemsByContestAndTrack(@Param("contestId") Long contestId, @Param("trackId") Long trackId, @Param("now") LocalDateTime now);

    @Query("SELECT i FROM ContestSubmissionItem i WHERE i.contest.id = :contestId AND i.endAt > :now AND i.contestTrack IS NULL")
    List<ContestSubmissionItem> findFutureCommonItemsByContest(@Param("contestId") Long contestId, @Param("now") LocalDateTime now);

    long countByContestId(Long contestId);
}
