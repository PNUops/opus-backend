package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestMemberFixture.createContestMember;
import static com.opus.opus.member.MemberFixture.createMemberWithRole;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.ALREADY_ASSIGNED_MEMBER;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.INVALID_TEAM_FOR_CONTEST;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.team.TeamFixture.createTeamWithContestId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestMemberCommandService;
import com.opus.opus.modules.contest.application.dto.request.StaffBatchAssignRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestMemberCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestMemberCommandService contestMemberCommandService;
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
    private Team teamA;
    private Team teamB;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        teamA = teamRepository.save(createTeamWithContestId(contest.getId()));
        teamB = teamRepository.save(createTeamWithContestId(contest.getId()));
        professor = memberRepository.save(createMemberWithRole("김교수", 1, ROLE_교수));
        mentor = memberRepository.save(createMemberWithRole("이멘토", 2, ROLE_외부멘토));
    }

    @Test
    @DisplayName("[성공] 여러 회원을 동일한 담당 팀으로 일괄 배정한다.")
    void 여러_회원을_동일한_담당_팀으로_일괄_배정한다() {
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId(), mentor.getId()),
                List.of(teamA.getId(), teamB.getId()));

        contestMemberCommandService.assignStaff(contest.getId(), request);

        final List<ContestMember> assigned = contestMemberRepository.findAllByContestId(contest.getId());
        assertThat(assigned).hasSize(2);
        assertThat(assigned)
                .extracting(ContestMember::getMemberId)
                .containsExactlyInAnyOrder(professor.getId(), mentor.getId());
        assertThat(assigned.get(0).getTeamIds())
                .containsExactlyInAnyOrder(teamA.getId(), teamB.getId());
    }

    @Test
    @DisplayName("[성공] 회원마다 독립된 담당 팀 목록을 가진다.")
    void 회원마다_독립된_담당_팀_목록을_가진다() {
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId(), mentor.getId()),
                List.of(teamA.getId()));

        contestMemberCommandService.assignStaff(contest.getId(), request);

        final List<ContestMember> assigned = contestMemberRepository.findAllByContestId(contest.getId());
        assertThat(assigned).allSatisfy(member -> assertThat(member.getTeamIds()).containsExactly(teamA.getId()));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회에는 배정할 수 없다.")
    void 존재하지_않는_대회에는_배정할_수_없다() {
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId()), List.of(teamA.getId()));

        assertThatThrownBy(() -> contestMemberCommandService.assignStaff(999L, request))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀이 포함되면 배정할 수 없다.")
    void 존재하지_않는_팀이_포함되면_배정할_수_없다() {
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId()), List.of(teamA.getId(), 999L));

        assertThatThrownBy(() -> contestMemberCommandService.assignStaff(contest.getId(), request))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 대회의 팀으로는 배정할 수 없다.")
    void 다른_대회의_팀으로는_배정할_수_없다() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContest());
        final Team otherTeam = teamRepository.save(createTeamWithContestId(otherContest.getId()));
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId()), List.of(otherTeam.getId()));

        assertThatThrownBy(() -> contestMemberCommandService.assignStaff(contest.getId(), request))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(INVALID_TEAM_FOR_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이미 배정된 회원은 다시 배정할 수 없다.")
    void 이미_배정된_회원은_다시_배정할_수_없다() {
        contestMemberRepository.save(createContestMember(contest, professor.getId(), List.of(teamA.getId())));
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId()), List.of(teamB.getId()));

        assertThatThrownBy(() -> contestMemberCommandService.assignStaff(contest.getId(), request))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(ALREADY_ASSIGNED_MEMBER.errorMessage());
    }
}
