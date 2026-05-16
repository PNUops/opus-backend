package com.opus.opus.modules.team.application;

import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.notification.application.convenience.NotificationConvenience;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import java.util.List;
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
    private final NotificationConvenience notificationConvenience;

    public void createTeamMember(final Member loginMember, final Long teamId, final String studentId,
                                 final String name, final TeamMemberRoleType roleType) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        teamMemberConvenience.validateTeamLeaderUnlessAdmin(teamId, loginMember);
        final Member member = memberConvenience.getOrCreateFakeMember(studentId, name);
        teamMemberConvenience.checkIsDuplicateTeamMember(teamId, member.getId());
        teamMemberRepository.save(
                TeamMember.builder()
                        .memberId(member.getId())
                        .team(team)
                        .roles(Set.of(roleType))
                        .build()
        );

        final List<Long> memberIds = teamMemberConvenience.findRealMemberIdsByTeamId(teamId);
        final String teamDisplayName = team.getTeamName() != null ? team.getTeamName() : team.getProjectName();
        notificationConvenience.sendTeamMemberJoinNotifications(memberIds, teamId, teamDisplayName);
    }

    public void deleteTeamMember(final Member loginMember, final Long teamId, final Long memberId) {
        teamConvenience.getValidateExistTeam(teamId);
        teamMemberConvenience.validateTeamLeaderUnlessAdmin(teamId, loginMember);
        final TeamMember teamMember = teamMemberConvenience.getValidateExistTeamMember(teamId, memberId);
        teamMemberRepository.delete(teamMember);
    }
}
