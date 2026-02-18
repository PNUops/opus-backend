package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamVoteRepository extends JpaRepository<TeamVote, Long> {

    Optional<TeamVote> findByMemberIdAndTeam(Long memberId, Team team);

    @Query("SELECT COUNT(tv) FROM TeamVote tv JOIN tv.team t " +
            "WHERE tv.memberId = :memberId AND tv.isVoted = true AND t.contestId = :contestId")
    long countMemberVotesInContest(Long memberId, Long contestId);

    List<TeamVote> findAllByTeamIdInOrderByCreatedAtDesc(final List<Long> teamIds);
}
