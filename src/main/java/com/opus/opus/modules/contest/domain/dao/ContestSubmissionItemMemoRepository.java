package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionItemMemo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestSubmissionItemMemoRepository extends JpaRepository<ContestSubmissionItemMemo, Long> {

    Optional<ContestSubmissionItemMemo> findBySubmissionItemId(Long submissionItemId);

    boolean existsBySubmissionItemId(Long submissionItemId);
}
