package com.opus.opus.modules.team.application.convenience;

import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.NOT_TEAM_LEADER;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_ALREADY_EXISTS;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberConvenience {

    private final TeamMemberRepository teamMemberRepository;

    public void checkIsDuplicateTeamMember(final Long teamId, final Long memberId) {
        if (teamMemberRepository.existsByTeamIdAndMemberId(teamId, memberId)) {
            throw new TeamMemberException(TEAM_MEMBER_ALREADY_EXISTS);
        }
    }

    public TeamMember getValidateExistTeamMember(final Long teamId, final Long memberId) {
        return teamMemberRepository.findByTeamIdAndMemberId(teamId, memberId)
                .orElseThrow(() -> new TeamMemberException(TEAM_MEMBER_NOT_FOUND_IN_TEAM));
    }


    public void validateTeamMemberUnlessAdmin(final Long teamId, final Member member) {
        if (!member.isAdmin()) {
            getValidateExistTeamMember(teamId, member.getId());
        }
    }

    public void validateTeamLeaderUnlessAdmin(final Long teamId, final Member member) {
        if (!member.isAdmin()) {
            final TeamMember teamMember = getValidateExistTeamMember(teamId, member.getId());
            if (!teamMember.getRoles().contains(TeamMemberRoleType.ROLE_팀장)) {
                throw new TeamMemberException(NOT_TEAM_LEADER);
            }
        }
    }
}
