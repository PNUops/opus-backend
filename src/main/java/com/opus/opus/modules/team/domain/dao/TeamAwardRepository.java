package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamAward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamAwardRepository extends JpaRepository<TeamAward, Long> {

    @Modifying
    @Query("DELETE FROM TeamAward ta WHERE ta.team.id = :teamId")
    void deleteAllByTeamId(@Param("teamId") Long teamId);
}
