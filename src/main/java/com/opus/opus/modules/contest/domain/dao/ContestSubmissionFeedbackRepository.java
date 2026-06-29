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

    long countBySubmissionIdIn(List<Long> submissionIds);

    // 멘토 조회용: 주어진 제출물들 중 해당 멘토가 피드백을 작성한 제출물 ID 목록
    @Query("""
            SELECT f.submission.id
            FROM ContestSubmissionFeedback f
            WHERE f.memberId = :memberId
              AND f.submission.id IN :submissionIds
            """)
    List<Long> findReviewedSubmissionIds(Long memberId, List<Long> submissionIds);
}