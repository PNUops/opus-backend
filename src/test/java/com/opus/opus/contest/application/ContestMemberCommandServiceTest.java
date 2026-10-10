package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestMemberFixture.createContestMember;
import static com.opus.opus.member.MemberFixture.createMemberWithEmailAndRoles;
import static com.opus.opus.member.MemberFixture.createMemberWithRole;
import static com.opus.opus.member.MemberFixture.createMemberWithUniqueNum;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.ALREADY_ASSIGNED_MEMBER;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_FOUND_CONTEST_MEMBER;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TEAM_NOT_IN_CONTEST;
import static com.opus.opus.team.TeamFixture.createTeamWithContestId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestMemberCommandService;
import com.opus.opus.modules.contest.application.dto.request.StaffBatchAssignRequest;
import com.opus.opus.modules.contest.application.dto.request.StaffTeamUpdateRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestMember;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.notification.application.event.StaffAssignedNotificationEvent;
import com.opus.opus.modules.notification.application.event.StaffAssignmentTeam;
import com.opus.opus.modules.notification.application.event.StaffPosition;
import com.opus.opus.modules.notification.application.event.StaffUnassignedNotificationEvent;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
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
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private ApplicationEvents applicationEvents;

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
        assertThat(assigned).allSatisfy(member -> assertThat(member.getTeamIds())
                .containsExactlyInAnyOrder(teamA.getId(), teamB.getId()));
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
                .isInstanceOf(TeamException.class)
                .hasMessage(TEAM_NOT_IN_CONTEST.errorMessage());
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

    @Test
    @DisplayName("[성공] 배정된 팀을 추가하고 삭제한다.")
    void 배정된_팀을_추가하고_삭제한다() {
        final ContestMember contestMember = contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(teamA.getId())));
        final StaffTeamUpdateRequest request = new StaffTeamUpdateRequest(
                List.of(teamB.getId()), List.of(teamA.getId()));

        contestMemberCommandService.updateAssignedTeams(contest.getId(), contestMember.getId(), request);

        assertThat(contestMemberRepository.findById(contestMember.getId()).orElseThrow().getTeamIds())
                .containsExactly(teamB.getId());
    }

    @Test
    @DisplayName("[성공] 이미 배정된 팀을 추가해도 중복되지 않는다.")
    void 이미_배정된_팀을_추가해도_중복되지_않는다() {
        final ContestMember contestMember = contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(teamA.getId(), teamB.getId())));
        final StaffTeamUpdateRequest request = new StaffTeamUpdateRequest(
                List.of(teamA.getId()), List.of());

        contestMemberCommandService.updateAssignedTeams(contest.getId(), contestMember.getId(), request);

        assertThat(contestMemberRepository.findById(contestMember.getId()).orElseThrow().getTeamIds())
                .containsExactlyInAnyOrder(teamA.getId(), teamB.getId());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 배정은 수정할 수 없다.")
    void 존재하지_않는_배정은_수정할_수_없다() {
        final StaffTeamUpdateRequest request = new StaffTeamUpdateRequest(
                List.of(teamA.getId()), List.of());

        assertThatThrownBy(() -> contestMemberCommandService.updateAssignedTeams(contest.getId(), 999L, request))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(NOT_FOUND_CONTEST_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 대회의 팀은 추가할 수 없다.")
    void 다른_대회의_팀은_추가할_수_없다() {
        final ContestMember contestMember = contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(teamA.getId())));
        final Contest otherContest = contestRepository.save(ContestFixture.createContest());
        final Team otherTeam = teamRepository.save(createTeamWithContestId(otherContest.getId()));
        final StaffTeamUpdateRequest request = new StaffTeamUpdateRequest(
                List.of(otherTeam.getId()), List.of());

        assertThatThrownBy(
                () -> contestMemberCommandService.updateAssignedTeams(contest.getId(), contestMember.getId(), request))
                .isInstanceOf(TeamException.class)
                .hasMessage(TEAM_NOT_IN_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 배정을 삭제하면 더 이상 조회되지 않는다.")
    void 배정을_삭제하면_더_이상_조회되지_않는다() {
        final ContestMember contestMember = contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(teamA.getId(), teamB.getId())));

        contestMemberCommandService.deleteAssignment(contest.getId(), contestMember.getId());

        assertThat(contestMemberRepository.findById(contestMember.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 배정은 삭제할 수 없다.")
    void 존재하지_않는_배정은_삭제할_수_없다() {
        assertThatThrownBy(() -> contestMemberCommandService.deleteAssignment(contest.getId(), 999L))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(NOT_FOUND_CONTEST_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 일괄 배정하면 교수는 지도교수, 외부멘토는 멘토 지정 알림 이벤트를 발행한다.")
    void 일괄_배정하면_교수는_지도교수_외부멘토는_멘토_지정_알림_이벤트를_발행한다() {
        final Member teammate = saveTeamMember(teamA, 5);
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId(), mentor.getId()), List.of(teamA.getId(), teamB.getId()));

        contestMemberCommandService.assignStaff(contest.getId(), request);

        final List<StaffAssignedNotificationEvent> events =
                applicationEvents.stream(StaffAssignedNotificationEvent.class).toList();
        assertThat(events).hasSize(2);
        assertThat(events).extracting(StaffAssignedNotificationEvent::staffId, StaffAssignedNotificationEvent::position)
                .containsExactlyInAnyOrder(
                        tuple(professor.getId(), StaffPosition.ADVISOR),
                        tuple(mentor.getId(), StaffPosition.MENTOR));
        assertThat(events).allSatisfy(event -> {
            assertThat(event.contestId()).isEqualTo(contest.getId());
            assertThat(event.teams()).extracting(StaffAssignmentTeam::teamId, StaffAssignmentTeam::memberIds)
                    .containsExactlyInAnyOrder(
                            tuple(teamA.getId(), List.of(teammate.getId())),
                            tuple(teamB.getId(), List.of()));
        });
    }

    @Test
    @DisplayName("[성공] 교수와 외부멘토 역할을 모두 가진 회원을 배정하면 지도교수와 멘토 지정 알림 이벤트를 모두 발행한다.")
    void 교수와_외부멘토_역할을_모두_가진_회원을_배정하면_지도교수와_멘토_지정_알림_이벤트를_모두_발행한다() {
        final Member staff = memberRepository.save(
                createMemberWithEmailAndRoles("staff@pusan.ac.kr", ROLE_교수, ROLE_외부멘토));
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(staff.getId()), List.of(teamA.getId()));

        contestMemberCommandService.assignStaff(contest.getId(), request);

        assertThat(applicationEvents.stream(StaffAssignedNotificationEvent.class))
                .extracting(StaffAssignedNotificationEvent::position)
                .containsExactlyInAnyOrder(StaffPosition.ADVISOR, StaffPosition.MENTOR);
    }

    @Test
    @DisplayName("[성공] 담당 팀을 수정하면 추가된 팀은 지정, 삭제된 팀은 해제 알림 이벤트를 발행한다.")
    void 담당_팀을_수정하면_추가된_팀은_지정_삭제된_팀은_해제_알림_이벤트를_발행한다() {
        final ContestMember contestMember = contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(teamA.getId())));
        final StaffTeamUpdateRequest request = new StaffTeamUpdateRequest(
                List.of(teamB.getId()), List.of(teamA.getId()));

        contestMemberCommandService.updateAssignedTeams(contest.getId(), contestMember.getId(), request);

        final List<StaffAssignedNotificationEvent> assignedEvents =
                applicationEvents.stream(StaffAssignedNotificationEvent.class).toList();
        assertThat(assignedEvents).hasSize(1);
        assertThat(assignedEvents.get(0).teams()).extracting(StaffAssignmentTeam::teamId)
                .containsExactly(teamB.getId());

        final List<StaffUnassignedNotificationEvent> unassignedEvents =
                applicationEvents.stream(StaffUnassignedNotificationEvent.class).toList();
        assertThat(unassignedEvents).hasSize(1);
        assertThat(unassignedEvents.get(0).staffId()).isEqualTo(professor.getId());
        assertThat(unassignedEvents.get(0).teams()).extracting(StaffAssignmentTeam::teamId)
                .containsExactly(teamA.getId());
    }

    @Test
    @DisplayName("[성공] 배정 상태가 바뀌지 않는 수정 요청이면 알림 이벤트를 발행하지 않는다.")
    void 배정_상태가_바뀌지_않는_수정_요청이면_알림_이벤트를_발행하지_않는다() {
        final ContestMember contestMember = contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(teamA.getId())));
        final StaffTeamUpdateRequest request = new StaffTeamUpdateRequest(
                List.of(teamA.getId()), List.of(teamB.getId()));

        contestMemberCommandService.updateAssignedTeams(contest.getId(), contestMember.getId(), request);

        assertThat(applicationEvents.stream(StaffAssignedNotificationEvent.class)).isEmpty();
        assertThat(applicationEvents.stream(StaffUnassignedNotificationEvent.class)).isEmpty();
    }

    @Test
    @DisplayName("[성공] 배정을 삭제하면 담당하던 모든 팀에 해제 알림 이벤트를 발행한다.")
    void 배정을_삭제하면_담당하던_모든_팀에_해제_알림_이벤트를_발행한다() {
        final ContestMember contestMember = contestMemberRepository.save(
                createContestMember(contest, mentor.getId(), List.of(teamA.getId(), teamB.getId())));

        contestMemberCommandService.deleteAssignment(contest.getId(), contestMember.getId());

        final List<StaffUnassignedNotificationEvent> events =
                applicationEvents.stream(StaffUnassignedNotificationEvent.class).toList();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).staffId()).isEqualTo(mentor.getId());
        assertThat(events.get(0).position()).isEqualTo(StaffPosition.MENTOR);
        assertThat(events.get(0).teams()).extracting(StaffAssignmentTeam::teamId)
                .containsExactlyInAnyOrder(teamA.getId(), teamB.getId());
    }

    @Test
    @DisplayName("[실패] 배정에 실패하면 지정 알림 이벤트를 발행하지 않는다.")
    void 배정에_실패하면_지정_알림_이벤트를_발행하지_않는다() {
        final StaffBatchAssignRequest request = new StaffBatchAssignRequest(
                List.of(professor.getId()), List.of(teamA.getId(), 999L));

        assertThatThrownBy(() -> contestMemberCommandService.assignStaff(contest.getId(), request))
                .isInstanceOf(TeamException.class);

        assertThat(applicationEvents.stream(StaffAssignedNotificationEvent.class)).isEmpty();
    }

    private Member saveTeamMember(final Team team, final int uniqueNum) {
        final Member teammate = memberRepository.save(createMemberWithUniqueNum(uniqueNum));
        teamMemberRepository.save(TeamMember.builder()
                .memberId(teammate.getId())
                .team(team)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀원))
                .build());
        return teammate;
    }
}
