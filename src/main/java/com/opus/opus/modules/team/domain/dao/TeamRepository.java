package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByContestId(Long contestId);

    boolean existsByTrackId(final Long trackId);

    List<Team> findByContestId(Long contestId);
}
