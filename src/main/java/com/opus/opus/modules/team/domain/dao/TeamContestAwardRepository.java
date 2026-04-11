package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.dao.projection.TeamAwardProjection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamContestAwardRepository extends JpaRepository<TeamContestAward, Long> {
    List<TeamContestAward> findByTeamId(final Long teamId);

    void deleteAllByContestAwardId(final Long contestAwardId);

    @Query("""
            SELECT tca.team.id AS teamId, ca.awardName AS awardName, ca.awardColor AS awardColor
            FROM TeamContestAward tca
            JOIN ContestAward ca ON tca.contestAwardId = ca.id
            WHERE tca.team.id IN :teamIds
            """)
    List<TeamAwardProjection> findTeamAwardsByTeamIds(@Param("teamIds") List<Long> teamIds);
}
