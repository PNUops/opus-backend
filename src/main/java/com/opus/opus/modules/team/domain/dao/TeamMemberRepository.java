package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamMember;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndMemberId(final Long teamId, final Long memberId);

    Optional<TeamMember> findByTeamIdAndMemberId(final Long teamId, final Long memberId);

    @Query("""
            SELECT new com.opus.opus.modules.team.domain.dao.MyProjectFlatResult(
                   c.id, c.contestName, t.id, t.teamName, t.projectName,
                   ct.trackName, ca.awardName, ca.awardColor)
            FROM TeamMember tm
            JOIN tm.team t
            JOIN Contest c ON c.id = t.contestId
            LEFT JOIN ContestTrack ct ON ct.id = t.trackId
            LEFT JOIN TeamContestAward tca ON tca.team = t
            LEFT JOIN ContestAward ca ON ca.id = tca.contestAwardId
            WHERE tm.memberId = :memberId
            ORDER BY c.id DESC
            """)
    List<MyProjectFlatResult> findMyProjectsWithAwards(Long memberId);

    @Query("SELECT tm.memberId FROM TeamMember tm JOIN Team t ON tm.team.id = t.id WHERE t.contestId = :contestId")
    Set<Long> findMemberIdsByContestId(Long contestId);

    @Query("SELECT DISTINCT tm.memberId FROM TeamMember tm JOIN Member m ON m.id = tm.memberId WHERE tm.team.id = :teamId AND m.isFake = false AND m.isDeleted = false")
    List<Long> findRealMemberIdsByTeamId(final Long teamId);
}
