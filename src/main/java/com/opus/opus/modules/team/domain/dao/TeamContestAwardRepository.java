package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamContestAward;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamContestAwardRepository extends JpaRepository<TeamContestAward, Long> {
    List<TeamContestAward> findByTeamId(final Long teamId);

    List<TeamContestAward> findByTeamIdIn(final List<Long> teamIds);

    @Query("""
            SELECT new com.opus.opus.modules.team.domain.dao.TeamAwardResult(
                tca.team.id, ca.awardName, ca.awardColor
            )
            FROM TeamContestAward tca
            JOIN ContestAward ca ON tca.contestAwardId = ca.id
            WHERE tca.team.id IN :teamIds
            """)
    List<TeamAwardResult> findTeamAwardsByTeamIds(@Param("teamIds") List<Long> teamIds);
}
