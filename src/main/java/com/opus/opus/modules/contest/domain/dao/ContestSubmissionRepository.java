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
}
