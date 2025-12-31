package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamLikeRepository extends JpaRepository<TeamLike, Long> {

    List<TeamLike> findAllByMemberIdAndTeamIn(final Long memberId, final List<Team> teams);

}
