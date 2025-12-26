package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamMemberCommandService {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final MemberConvenience memberConvenience;

    public void addTeamMember(final Long teamId, final String studentId, final String name) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        final Member member = memberConvenience.getOrCreateFakeMember(studentId, name);
        teamMemberConvenience.checkIsDuplicateTeamMember(teamId, member.getId());
        teamMemberRepository.save(
                TeamMember.builder()
                        .memberId(member.getId())
                        .team(team)
                        .roles(Set.of(ROLE_팀원))
                        .build()
        );
    }

    public void removeTeamMember(final Long teamId, final Long memberId) {
        teamConvenience.getValidateExistTeam(teamId);
        memberConvenience.getValidateExistMember(memberId);
        final TeamMember teamMember = teamMemberConvenience.getValidateExistTeamMember(teamId, memberId);
        teamMemberRepository.delete(teamMember);
    }
}
