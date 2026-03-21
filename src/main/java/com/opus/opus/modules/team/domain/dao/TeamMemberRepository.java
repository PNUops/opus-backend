package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamMember;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndMemberId(final Long teamId, final Long memberId);

    Optional<TeamMember> findByTeamIdAndMemberId(final Long teamId, final Long memberId);

    @Query("SELECT tm.memberId FROM TeamMember tm JOIN Team t ON tm.team.id = t.id WHERE t.contestId = :contestId")
    Set<Long> findMemberIdsByContestId(Long contestId);
}
