package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestTemplateRepository extends JpaRepository<ContestTemplate, Long> {

    Optional<ContestTemplate> findByContestId(final Long contestId);

}
