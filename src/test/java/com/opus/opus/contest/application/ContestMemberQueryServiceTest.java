package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestMemberFixture.createContestMember;
import static com.opus.opus.member.MemberFixture.createMemberWithRole;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.INVALID_MEMBER_TYPE;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static com.opus.opus.team.TeamFixture.createTeamWithContestIdAndTeamName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestMemberQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse.TeamInfo;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestMemberQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestMemberQueryService contestMemberQueryService;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ContestMemberRepository contestMemberRepository;

    private Contest contest;
    private Member professor;
    private Member mentor;
    private Team developTeam;
    private Team planningTeam;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        developTeam = teamRepository.save(createTeamWithContestIdAndTeamName(contest.getId(), "개발 1팀"));
        planningTeam = teamRepository.save(createTeamWithContestIdAndTeamName(contest.getId(), "운영 기획팀"));
        professor = memberRepository.save(createMemberWithRole("김교수", 1, ROLE_교수));
        mentor = memberRepository.save(createMemberWithRole("이멘토", 2, ROLE_외부멘토));

        contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(developTeam.getId(), planningTeam.getId())));
        contestMemberRepository.save(createContestMember(contest, mentor.getId(), List.of(developTeam.getId())));
    }

    @Test
    @DisplayName("[성공] 배정된 회원과 담당 팀 정보를 함께 조회한다.")
    void 배정된_회원과_담당_팀_정보를_함께_조회한다() {
        final List<ContestStaffResponse> responses = contestMemberQueryService.getAssignedStaff(contest.getId(), null,
                null);

        final ContestStaffResponse professorStaff = findByMemberId(responses, professor.getId());
        assertThat(responses).hasSize(2);
        assertThat(professorStaff.name()).isEqualTo("김교수");
        assertThat(professorStaff.email()).isEqualTo(professor.getEmail());
        assertThat(professorStaff.roleType()).isEqualTo(ROLE_교수.name());
        assertThat(professorStaff.teams())
                .extracting(TeamInfo::teamName)
                .containsExactlyInAnyOrder("개발 1팀", "운영 기획팀");
    }

    @Test
    @DisplayName("[성공] memberType으로 회원 유형을 필터링한다.")
    void memberType으로_회원_유형을_필터링한다() {
        final List<ContestStaffResponse> responses = contestMemberQueryService.getAssignedStaff(contest.getId(),
                ROLE_외부멘토.name(), null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).memberId()).isEqualTo(mentor.getId());
        assertThat(responses.get(0).roleType()).isEqualTo(ROLE_외부멘토.name());
    }

    @Test
    @DisplayName("[성공] 이름으로 검색한다.")
    void 이름으로_검색한다() {
        final List<ContestStaffResponse> responses = contestMemberQueryService.getAssignedStaff(contest.getId(), null,
                "이멘토");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).memberId()).isEqualTo(mentor.getId());
    }

    @Test
    @DisplayName("[성공] 담당 팀 이름으로 검색한다.")
    void 담당_팀_이름으로_검색한다() {
        final List<ContestStaffResponse> responses = contestMemberQueryService.getAssignedStaff(contest.getId(), null,
                "기획");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).memberId()).isEqualTo(professor.getId());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 배정 목록은 조회할 수 없다.")
    void 존재하지_않는_대회의_배정_목록은_조회할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() -> contestMemberQueryService.getAssignedStaff(invalidContestId, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 유효하지 않은 memberType이면 예외가 발생한다.")
    void 유효하지_않은_memberType이면_예외가_발생한다() {
        assertThatThrownBy(() -> contestMemberQueryService.getAssignedStaff(contest.getId(), "ROLE_INVALID", null))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(INVALID_MEMBER_TYPE.errorMessage());
    }

    private ContestStaffResponse findByMemberId(final List<ContestStaffResponse> responses, final Long memberId) {
        return responses.stream()
                .filter(response -> response.memberId().equals(memberId))
                .findFirst()
                .orElseThrow();
    }
}
