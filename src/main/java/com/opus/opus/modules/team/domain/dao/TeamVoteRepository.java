package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamVoteRepository extends JpaRepository<TeamVote, Long> {

    Optional<TeamVote> findByMemberIdAndTeam(Long memberId, Team team);

    @Query("SELECT COUNT(tv) FROM TeamVote tv JOIN tv.team t " +
            "WHERE tv.memberId = :memberId AND tv.isVoted = true AND t.contestId = :contestId")
    long countMemberVotesInContest(Long memberId, Long contestId);

    @Query("SELECT new com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse(" +
            "team.id, team.teamName, team.projectName, track.trackName, COUNT(vote)) " +
            "FROM TeamVote vote " +
            "JOIN vote.team team " +
            "LEFT JOIN ContestTrack track ON team.trackId = track.id " +
            "WHERE team.contestId = :contestId AND vote.isVoted = true " +
            "GROUP BY team.id, team.teamName, team.projectName, track.trackName " +
            "ORDER BY COUNT(vote) DESC")
    List<ContestRankingResponse> countVotesPerTeamByContest(Long contestId);

    @Query("SELECT COUNT(vote) " +
            "FROM TeamVote vote " +
            "JOIN vote.team team " +
            "WHERE team.contestId = :contestId AND vote.isVoted = true")
    long countTotalVotesByContest(Long contestId);

    @Query("SELECT COUNT(DISTINCT vote.memberId) " +
            "FROM TeamVote vote " +
            "JOIN vote.team team " +
            "WHERE team.contestId = :contestId AND vote.isVoted = true")
    long countTotalVotersByContest(Long contestId);
}
