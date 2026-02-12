package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    boolean existsByContestId(Long contestId);

    boolean existsByTrackId(final Long trackId);

    @Query("SELECT new com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse(" +
            "team.id, team.teamName, team.projectName, track.trackName, COUNT(vote.id)) " +
            "FROM Team team " +
            "LEFT JOIN TeamVote vote ON vote.team = team AND vote.isVoted = true " +
            "LEFT JOIN ContestTrack track ON team.trackId = track.id " +
            "WHERE team.contestId = :contestId " +
            "GROUP BY team.id, team.teamName, team.projectName, track.trackName " +
            "ORDER BY COUNT(vote.id) DESC")
    List<ContestRankingResponse> findTeamRankingByContestId(Long contestId); // 특정 대회에 속한 모든 팀을, 투표 수 기준 내림차순으로 조회 (투표 수 0인 팀도 포함)
}
