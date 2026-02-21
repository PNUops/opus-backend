package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    List<TeamRankingResult> findTeamRankingByContestId(Long contestId); // 특정 대회에 속한 모든 팀을, 투표 수 기준 내림차순으로 조회 (투표 수 0인 팀도 포함)

    @Query("SELECT new com.opus.opus.modules.team.domain.dao.TeamSubmissionResult(" +
            "team.id, team.teamName, team.projectName, track.trackName, team.isSubmitted) " +
            "FROM Team team " +
            "LEFT JOIN ContestTrack track ON team.trackId = track.id " +
            "WHERE team.contestId = :contestId " +
            "ORDER BY team.id ASC")
    List<TeamSubmissionResult> findTeamSubmissionsByContestId(final Long contestId);
}
