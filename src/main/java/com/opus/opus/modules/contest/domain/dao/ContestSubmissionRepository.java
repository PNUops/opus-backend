package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestSubmissionRepository extends JpaRepository<ContestSubmission, Long> {

    boolean existsByTeamIdAndSubmissionItem(final Long teamId, final ContestSubmissionItem submissionItem);
}
