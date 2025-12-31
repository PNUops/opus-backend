package com.opus.opus.modules.contest.domain.dao;

import com.opus.opus.modules.contest.domain.ContestTeamTemplate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContestTeamTemplateRepository extends JpaRepository<ContestTeamTemplate, Long> {

    Optional<ContestTeamTemplate> findByContestId(final Long contestId);

}
