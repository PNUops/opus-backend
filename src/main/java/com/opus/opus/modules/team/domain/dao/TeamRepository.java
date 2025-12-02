package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByContestId(final Long contestId);

    boolean existsByTrackId(final Long trackId);
}
