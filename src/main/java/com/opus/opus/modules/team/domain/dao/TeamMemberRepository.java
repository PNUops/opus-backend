package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndMemberId(final Long teamId, final Long memberId);

    Optional<TeamMember> findByTeamIdAndMemberId(final Long teamId, final Long memberId);
}
