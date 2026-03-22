package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByContestId(final Long contestId);

    boolean existsByTrackId(final Long trackId);

    List<Team> findAllByContestId(final Long contestId);

    @Query("SELECT new com.opus.opus.modules.team.domain.dao.TeamRankingResult(" +
            "team.id, team.teamName, team.projectName, track.trackName, COUNT(vote.id)) " +
            "FROM Team team " +
            "LEFT JOIN TeamVote vote ON vote.team = team AND vote.isVoted = true " +
            "LEFT JOIN ContestTrack track ON team.trackId = track.id " +
            "WHERE team.contestId = :contestId " +
            "GROUP BY team.id, team.teamName, team.projectName, track.trackName " +
            "ORDER BY COUNT(vote.id) DESC, team.id ASC")
    List<TeamRankingResult> findTeamRankingByContestId(
            Long contestId); // 특정 대회에 속한 모든 팀을, 투표 수 기준 내림차순으로 조회 (투표 수 0인 팀도 포함)

    @Query("""
             SELECT COALESCE(MAX(t.itemOrder), 0)
             FROM Team t
             WHERE t.contestId = :contestId
               AND t.isDeleted = false
            """)
    int findMaxItemOrderByContestId(@Param("contestId") Long contestId);

    @Modifying
    @Query("""
             UPDATE Team t
             SET t.itemOrder = t.itemOrder - 1
             WHERE t.contestId = :contestId
               AND t.itemOrder > :deletedOrder
               AND t.isDeleted = false
            """)
    void updateItemOrderAfterDeletion(@Param("contestId") Long contestId, @Param("deletedOrder") int deletedOrder);
}
