package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamContestAward;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamAwardRepository extends JpaRepository<TeamContestAward, Long> {
    List<TeamContestAward> findByTeamId(Long teamId);
}
