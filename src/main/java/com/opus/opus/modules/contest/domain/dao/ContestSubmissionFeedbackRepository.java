package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContestSubmissionFeedbackRepository extends JpaRepository<ContestSubmissionFeedback, Long> {

    List<ContestSubmissionFeedback> findAllBySubmissionItemIdAndTeamIdOrderByIdDesc(final Long submissionItemId,
                                                                                    final Long teamId);

    Optional<ContestSubmissionFeedback> findBySubmissionItemIdAndTeamIdAndMemberId(final Long submissionItemId,
                                                                                   final Long teamId,
                                                                                   final Long memberId);

    Optional<ContestSubmissionFeedback> findByIdAndSubmissionItemIdAndTeamId(final Long id,
                                                                             final Long submissionItemId,
                                                                             final Long teamId);

    long countByTeamIdAndIsReadFalseAndSubmissionItem_IsDeletedFalse(final Long teamId);

    Optional<ContestSubmissionFeedback> findTopByTeamIdAndSubmissionItem_IsDeletedFalseOrderByCreatedAtDesc(
            final Long teamId);

    long countBySubmissionItemIdAndTeamId(final Long submissionItemId, final Long teamId);

    long countByTeamIdAndSubmissionItem_IsDeletedFalse(final Long teamId);

    @Query("""
            SELECT f.submissionItem.id
            FROM ContestSubmissionFeedback f
            WHERE f.memberId = :memberId
              AND f.teamId = :teamId
              AND f.submissionItem.id IN :submissionItemIds
            """)
    List<Long> findReviewedSubmissionItemIds(final Long memberId, final Long teamId,
                                             final List<Long> submissionItemIds);
}
