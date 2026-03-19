package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamLikeRepository extends JpaRepository<TeamLike, Long> {

    Optional<TeamLike> findByMemberIdAndTeam(Long memberId, Team team);

    long countByIsLikedTrue();
}
