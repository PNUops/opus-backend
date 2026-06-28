package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestSubmissionItemRepository extends JpaRepository<ContestSubmissionItem, Long> {

    List<ContestSubmissionItem> findAllByContestIdOrderByUpdatedAtDesc(final Long contestId);

    long countByContestId(Long contestId);
}
