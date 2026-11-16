package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestSubmissionMemoRepository extends JpaRepository<ContestSubmissionMemo, Long> {

    Optional<ContestSubmissionMemo> findBySubmissionItemIdAndTeamId(final Long submissionItemId, final Long teamId);

    boolean existsBySubmissionItemIdAndTeamId(final Long submissionItemId, final Long teamId);
}
