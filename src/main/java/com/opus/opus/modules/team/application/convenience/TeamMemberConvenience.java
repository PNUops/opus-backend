package com.opus.opus.modules.team.application.convenience;

import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀장;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.NOT_TEAM_LEADER;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_ALREADY_EXISTS;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;

import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import java.util.List;
import java.util.Set;
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

    public TeamMember save(final TeamMember teamMember) {
        return teamMemberRepository.save(teamMember);
    }

    public void saveTeamMember(final Long memberId, final Team team, final TeamMemberRoleType roleType) {
        final TeamMember teamMember = TeamMember.builder()
                .memberId(memberId)
                .team(team)
                .roles(Set.of(roleType))
                .build();
        teamMemberRepository.save(teamMember);
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

    /*
        [팀장/팀원 권한 검증 위치 변경]
        ROLE_팀장, ROLE_팀원을 JwtProvider roles에 추가해 @Secured로 검증하는 방식은
        매 요청마다 불필요한 TeamMember DB 조회가 발생하고, 해당 권한이 필요한 API도 많지 않아
        @Secured는 ROLE_회원만 검증하고, 팀장/팀원 여부는 Service 레이어에서 검증하는 방식으로 결정
     */
    public void validateTeamLeaderUnlessAdmin(final Long teamId, final Member member) {
        if (!member.isAdmin()) {
            final TeamMember teamMember = getValidateExistTeamMember(teamId, member.getId());
            if (!teamMember.getRoles().contains(ROLE_팀장)) {
                throw new TeamMemberException(NOT_TEAM_LEADER);
            }
        }
    }

    public Set<Long> findMemberIdsByContestId(final Long contestId) {
        return teamMemberRepository.findMemberIdsByContestId(contestId);
    }

    public List<Long> findRealMemberIdsByTeamId(final Long teamId) {
        return teamMemberRepository.findRealMemberIdsByTeamId(teamId);
    }
}
