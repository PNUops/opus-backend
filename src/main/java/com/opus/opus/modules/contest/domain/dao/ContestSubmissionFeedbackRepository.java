package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContestSubmissionFeedbackRepository extends JpaRepository<ContestSubmissionFeedback, Long> {

    List<ContestSubmissionFeedback> findAllBySubmissionIdOrderByIdDesc(Long submissionId);

    Optional<ContestSubmissionFeedback> findBySubmissionIdAndMemberId(Long submissionId, Long memberId);

    long countBySubmission_TeamIdAndIsReadFalse(Long teamId);

    Optional<ContestSubmissionFeedback> findTopBySubmission_TeamIdOrderByCreatedAtDesc(Long teamId);

    long countBySubmissionId(Long submissionId);

    long countBySubmissionIdIn(List<Long> submissionIds);

    @Query("""
            SELECT f.submission.id
            FROM ContestSubmissionFeedback f
            WHERE f.memberId = :memberId
              AND f.submission.id IN :submissionIds
            """)
    List<Long> findReviewedSubmissionIds(Long memberId, List<Long> submissionIds);
}
