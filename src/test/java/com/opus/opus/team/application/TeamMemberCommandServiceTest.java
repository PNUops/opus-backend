package com.opus.opus.team.application;

import static com.opus.opus.modules.member.exception.MemberExceptionType.MISMATCH_STUDENT_ID_AND_NAME;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀장;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.application.TeamMemberCommandService;
import com.opus.opus.modules.team.application.dto.request.TeamMemberCreateRequest;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.team.TeamFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TeamMemberCommandServiceTest extends IntegrationTest {

    @Autowired
    private TeamMemberCommandService teamMemberCommandService;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Team team;
    private Member member;
    private TeamMemberCreateRequest request;

    @BeforeEach
    void setUp() {
        team = teamRepository.save(TeamFixture.createTeam());
        member = memberRepository.save(MemberFixture.createMember());
        request = new TeamMemberCreateRequest("이사람", "123456789", ROLE_팀원);
    }

    @Test
    @DisplayName("[성공] 회원가입한 학번과 이름이 없으면 가짜 회원을 생성 후 팀원으로 추가한다.")
    void 회원가입한_학번과_이름이_없으면_가짜_회원을_생성_후_팀원으로_추가한다() {
        final String notExistStudentId = "202654321";
        final String notExistStudentName = "문스옵";

        teamMemberCommandService.createTeamMember(team.getId(), notExistStudentId, notExistStudentName,
                request.roleType());

        final Member fakeMember = memberRepository.findByStudentId(notExistStudentId).orElseThrow();
        assertThat(fakeMember.getStudentId()).isEqualTo(notExistStudentId);

        final TeamMember teamMember = teamMemberRepository.findByTeamIdAndMemberId(team.getId(), fakeMember.getId())
                .get();
        assertThat(teamMember.getMemberId()).isEqualTo(fakeMember.getId());
        assertThat(teamMember.getTeam().getId()).isEqualTo(team.getId());
    }

    @Test
    @DisplayName("[성공] 이미 회원가입한 회원의 학번과 이름으로 팀원으로 추가될 때 기존 회원을 팀원으로 추가한다")
    void 이미_회원가입한_회원의_학번과_이름으로_팀원으로_추가될_때_기존_회원을_팀원으로_추가한다() {
        final String signedUpStudentId = member.getStudentId();
        final String signedUpStudentName = member.getName();

        teamMemberCommandService.createTeamMember(team.getId(), signedUpStudentId, signedUpStudentName,
                request.roleType());

        final TeamMember teamMember = teamMemberRepository.findByTeamIdAndMemberId(team.getId(), member.getId())
                .orElseThrow();
        assertThat(teamMember.getMemberId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("[실패] 이미 회원가입한 회원의 학번인데 저장된 이름과 같지 않으면 팀원으로 추가 불가하다.")
    void 이미_회원가입한_회원의_학번인데_저장된_이름과_같지_않으면_팀원으로_추가_불가하다() {
        final String studentId = member.getStudentId();
        final String wrongStudentName = "이옵스아님";

        assertThatThrownBy(() -> {
            teamMemberCommandService.createTeamMember(team.getId(), studentId, wrongStudentName, request.roleType());
        }).isInstanceOf(MemberException.class).hasMessage(MISMATCH_STUDENT_ID_AND_NAME.errorMessage());
    }

    @Test
    @DisplayName("[성공] roleType이 팀장이라면 팀장으로 저장된다.")
    void roleType이_팀장이라면_팀장으로_저장된다() {
        final TeamMemberCreateRequest teamLeaderRequest = new TeamMemberCreateRequest("나팀장", "202798743", ROLE_팀장);

        teamMemberCommandService.createTeamMember(team.getId(), teamLeaderRequest.memberStudentId(),
                teamLeaderRequest.memberName(), teamLeaderRequest.roleType());

        final Member addedMember = memberRepository.findByStudentId(teamLeaderRequest.memberStudentId())
                .orElseThrow();
        final TeamMember teamLeader = teamMemberRepository.findByTeamIdAndMemberId(team.getId(), addedMember.getId())
                .orElseThrow();
        assertThat(teamLeader.getRoles()).containsExactly(teamLeaderRequest.roleType());
    }

    @Test
    @DisplayName("[성공] 팀원이 정상적으로 삭제된다.")
    void 팀원이_정상적으로_삭제된다() {
        teamMemberCommandService.createTeamMember(team.getId(), request.memberStudentId(), request.memberName(),
                request.roleType());
        final Member addedMember = memberRepository.findByStudentId(request.memberStudentId()).get();

        teamMemberCommandService.deleteTeamMember(team.getId(), addedMember.getId());

        assertThat(teamMemberRepository.existsByTeamIdAndMemberId(team.getId(), addedMember.getId())).isFalse();
    }

    @Test
    @DisplayName("[실패] 팀원 목록에 팀원 정보가 없다면 팀원 삭제 불가하다.")
    void 팀원_목록에_팀원_정보가_없다면_팀원_삭제_불가하다() {
        final Team otherTeam = teamRepository.save(TeamFixture.createTeam());

        assertThatThrownBy(() -> {
            teamMemberCommandService.deleteTeamMember(otherTeam.getId(), member.getId());
        }).isInstanceOf(TeamMemberException.class).hasMessage(TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }
}
