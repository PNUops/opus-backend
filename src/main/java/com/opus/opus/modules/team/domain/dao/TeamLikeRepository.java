package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamLikeRepository extends JpaRepository<TeamLike, Long> {

    Optional<TeamLike> findByMemberIdAndTeam(Long memberId, Team team);

    @Query("""
                SELECT tl
                FROM TeamLike tl
                JOIN tl.team t
                WHERE tl.memberId = :memberId AND t.contestId = :contestId
            """)
    List<TeamLike> findAllByMemberIdAndContestId(Long memberId, Long contestId);
}
