package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestSubmissionRepository extends JpaRepository<ContestSubmission, Long> {

    boolean existsByTeamIdAndSubmissionItem(final Long teamId, final ContestSubmissionItem submissionItem);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE ContestSubmission s SET s.updatedAt = CURRENT_TIMESTAMP WHERE s.id = :submissionId")
    void touchUpdatedAt(@Param("submissionId") final Long submissionId);
}
