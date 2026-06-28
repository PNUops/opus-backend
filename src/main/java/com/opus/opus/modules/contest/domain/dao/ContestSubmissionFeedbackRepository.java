package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestSubmissionFeedbackRepository extends JpaRepository<ContestSubmissionFeedback, Long> {

    List<ContestSubmissionFeedback> findAllBySubmissionIdOrderByIdDesc(Long submissionId);

    Optional<ContestSubmissionFeedback> findBySubmissionIdAndMemberId(Long submissionId, Long memberId);

    long countBySubmission_TeamIdAndIsReadFalse(Long teamId);

    Optional<ContestSubmissionFeedback> findTopBySubmission_TeamIdOrderByCreatedAtDesc(Long teamId);
}
