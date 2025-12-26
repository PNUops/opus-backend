package com.opus.opus.modules.team.domain.dao;

import com.opus.opus.modules.team.domain.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    boolean existsByTeamIdAndMemberId(final Long teamId, final Long memberId);
}
