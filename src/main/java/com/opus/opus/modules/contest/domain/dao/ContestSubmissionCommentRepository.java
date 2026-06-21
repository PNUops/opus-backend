package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestSubmissionCommentRepository extends JpaRepository<ContestSubmissionComment, Long> {

    List<ContestSubmissionComment> findAllBySubmissionIdOrderByIdDesc(Long submissionId);
}
