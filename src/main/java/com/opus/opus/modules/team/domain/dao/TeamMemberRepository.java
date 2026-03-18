package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamMember;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndMemberId(final Long teamId, final Long memberId);

    Optional<TeamMember> findByTeamIdAndMemberId(final Long teamId, final Long memberId);

    List<TeamMember> findAllByMemberId(final Long memberId);

    @Query("""
            SELECT new com.opus.opus.modules.team.domain.dao.MyProjectFlatResult(
                   c.id, c.contestName,
                   t.id, t.teamName, t.projectName,
                   ct.trackName,
                   ca.awardName, ca.awardColor)
            FROM TeamMember tm
            JOIN tm.team t
            JOIN Contest c ON c.id = t.contestId
            LEFT JOIN ContestTrack ct ON ct.id = t.trackId
            LEFT JOIN TeamContestAward tca ON tca.team = t
            LEFT JOIN ContestAward ca ON ca.id = tca.contestAwardId
            WHERE tm.memberId = :memberId
            """)
    List<MyProjectFlatResult> findMyProjectsWithAwards(Long memberId);
}
