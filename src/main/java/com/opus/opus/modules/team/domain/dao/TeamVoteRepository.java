package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamVoteRepository extends JpaRepository<TeamVote, Long> {
    List<TeamVote> findAllByMemberIdAndTeamIn(final Long memberId, final List<Team> teams);
}
