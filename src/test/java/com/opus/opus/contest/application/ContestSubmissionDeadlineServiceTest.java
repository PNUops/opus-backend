package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestSubmissionItemFixture.createSubmissionItem;
import static com.opus.opus.team.TeamFixture.createTeamWithContestId;
import static com.opus.opus.team.TeamFixture.createTeamWithContestIdAndTrackId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionDeadlineService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.notification.application.event.SubmissionDeadlineNotificationEvent;
import com.opus.opus.modules.notification.application.event.SubmissionDeadlineTeam;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
public class ContestSubmissionDeadlineServiceTest extends IntegrationTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);
    private static final LocalTime DEADLINE_TIME = LocalTime.of(23, 59);

    @Autowired
    private ContestSubmissionDeadlineService contestSubmissionDeadlineService;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private ContestSubmissionItemRepository contestSubmissionItemRepository;
    @Autowired
    private ContestSubmissionRepository contestSubmissionRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ApplicationEvents applicationEvents;

    private Contest contest;
    private Team teamA;
    private Team teamB;
    private Member memberA;
    private Member memberB;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        teamA = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), null));
        teamB = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), null));
        memberA = saveTeamMember(teamA, 1);
        memberB = saveTeamMember(teamB, 2);
    }

    @Test
    @DisplayName("[성공] 마감 3일 전인 제출 항목은 미제출 팀의 팀원에게 D-3 제출 마감 알림 이벤트를 발행한다.")
    void 마감_3일_전인_제출_항목은_미제출_팀의_팀원에게_D_3_제출_마감_알림_이벤트를_발행한다() {
        final ContestSubmissionItem submissionItem = saveSubmissionItem(null, TODAY.plusDays(3).atTime(DEADLINE_TIME));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        final SubmissionDeadlineNotificationEvent event = getOnlyEvent(submissionItem);
        assertThat(event.contestId()).isEqualTo(contest.getId());
        assertThat(event.submissionItemName()).isEqualTo(submissionItem.getName());
        assertThat(event.daysLeft()).isEqualTo(3);
        assertThat(event.teams()).extracting(SubmissionDeadlineTeam::teamId, SubmissionDeadlineTeam::memberIds)
                .containsExactlyInAnyOrder(
                        tuple(teamA.getId(), List.of(memberA.getId())),
                        tuple(teamB.getId(), List.of(memberB.getId())));
    }

    @Test
    @DisplayName("[성공] 마감 1일 전인 제출 항목은 D-1 제출 마감 알림 이벤트를 발행한다.")
    void 마감_1일_전인_제출_항목은_D_1_제출_마감_알림_이벤트를_발행한다() {
        final ContestSubmissionItem submissionItem = saveSubmissionItem(null, TODAY.plusDays(1).atTime(DEADLINE_TIME));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(submissionItem).daysLeft()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 마감 시각과 관계없이 마감 날짜가 3일 뒤이면 대상이 된다.")
    void 마감_시각과_관계없이_마감_날짜가_3일_뒤이면_대상이_된다() {
        final ContestSubmissionItem startOfDayItem = saveSubmissionItem(null, TODAY.plusDays(3).atStartOfDay());
        final ContestSubmissionItem endOfDayItem = saveSubmissionItem(null, TODAY.plusDays(3).atTime(23, 59, 59));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(startOfDayItem).daysLeft()).isEqualTo(3);
        assertThat(getOnlyEvent(endOfDayItem).daysLeft()).isEqualTo(3);
    }

    @Test
    @DisplayName("[성공] 마감 날짜가 D-3, D-1이 아닌 제출 항목은 제출 마감 알림 이벤트를 발행하지 않는다.")
    void 마감_날짜가_D_3_D_1이_아닌_제출_항목은_제출_마감_알림_이벤트를_발행하지_않는다() {
        final List<ContestSubmissionItem> submissionItems = List.of(
                saveSubmissionItem(null, TODAY.atTime(DEADLINE_TIME)),
                saveSubmissionItem(null, TODAY.plusDays(2).atTime(DEADLINE_TIME)),
                saveSubmissionItem(null, TODAY.plusDays(4).atStartOfDay()));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        submissionItems.forEach(submissionItem -> assertThat(findEvents(submissionItem)).isEmpty());
    }

    @Test
    @DisplayName("[성공] 이미 제출한 팀은 제출 마감 알림 대상에서 제외된다.")
    void 이미_제출한_팀은_제출_마감_알림_대상에서_제외된다() {
        final ContestSubmissionItem submissionItem = saveSubmissionItem(null, TODAY.plusDays(3).atTime(DEADLINE_TIME));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(teamA.getId(), submissionItem));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(submissionItem).teams()).extracting(SubmissionDeadlineTeam::teamId)
                .containsExactly(teamB.getId());
    }

    @Test
    @DisplayName("[성공] 모든 팀이 제출했으면 제출 마감 알림 이벤트를 발행하지 않는다.")
    void 모든_팀이_제출했으면_제출_마감_알림_이벤트를_발행하지_않는다() {
        final ContestSubmissionItem submissionItem = saveSubmissionItem(null, TODAY.plusDays(3).atTime(DEADLINE_TIME));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(teamA.getId(), submissionItem));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(teamB.getId(), submissionItem));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(findEvents(submissionItem)).isEmpty();
    }

    @Test
    @DisplayName("[성공] 트랙이 지정된 제출 항목은 해당 트랙의 팀만 대상이 된다.")
    void 트랙이_지정된_제출_항목은_해당_트랙의_팀만_대상이_된다() {
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final Team trackTeam = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        saveTeamMember(trackTeam, 3);
        final ContestSubmissionItem submissionItem = saveSubmissionItem(track, TODAY.plusDays(3).atTime(DEADLINE_TIME));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(submissionItem).teams()).extracting(SubmissionDeadlineTeam::teamId)
                .containsExactly(trackTeam.getId());
    }

    @Test
    @DisplayName("[성공] 다른 대회의 팀은 제출 마감 알림 대상에서 제외된다.")
    void 다른_대회의_팀은_제출_마감_알림_대상에서_제외된다() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContest());
        final Team otherTeam = teamRepository.save(createTeamWithContestId(otherContest.getId()));
        saveTeamMember(otherTeam, 3);
        final ContestSubmissionItem submissionItem = saveSubmissionItem(null, TODAY.plusDays(3).atTime(DEADLINE_TIME));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(submissionItem).teams()).extracting(SubmissionDeadlineTeam::teamId)
                .containsExactlyInAnyOrder(teamA.getId(), teamB.getId());
    }

    @Test
    @DisplayName("[성공] 지각 제출을 허용하지 않는 제출 항목도 제출 마감 알림 대상이 된다.")
    void 지각_제출을_허용하지_않는_제출_항목도_제출_마감_알림_대상이_된다() {
        final ContestSubmissionItem submissionItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(
                        contest, TODAY.plusDays(1).atTime(DEADLINE_TIME), false));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(submissionItem).daysLeft()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 제출 시작 전인 제출 항목도 제출 마감 알림 대상이 된다.")
    void 제출_시작_전인_제출_항목도_제출_마감_알림_대상이_된다() {
        final ContestSubmissionItem submissionItem = contestSubmissionItemRepository.save(createSubmissionItem(
                contest, null, "중간보고서", TODAY.plusDays(2).atStartOfDay(), TODAY.plusDays(3).atTime(DEADLINE_TIME)));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(submissionItem).daysLeft()).isEqualTo(3);
    }

    @Test
    @DisplayName("[성공] 가짜 계정 팀원은 제출 마감 알림 대상에서 제외된다.")
    void 가짜_계정_팀원은_제출_마감_알림_대상에서_제외된다() {
        final Member fakeMember = saveTeamMember(teamA, 3);
        fakeMember.markAsFakeMember();
        final ContestSubmissionItem submissionItem = saveSubmissionItem(null, TODAY.plusDays(3).atTime(DEADLINE_TIME));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(getOnlyEvent(submissionItem).teams())
                .filteredOn(team -> team.teamId().equals(teamA.getId()))
                .flatExtracting(SubmissionDeadlineTeam::memberIds)
                .containsExactly(memberA.getId());
    }

    @Test
    @DisplayName("[성공] 미제출 팀에 팀원이 없으면 제출 마감 알림 이벤트를 발행하지 않는다.")
    void 미제출_팀에_팀원이_없으면_제출_마감_알림_이벤트를_발행하지_않는다() {
        final Contest emptyContest = contestRepository.save(ContestFixture.createContest());
        teamRepository.save(createTeamWithContestId(emptyContest.getId()));
        final ContestSubmissionItem submissionItem = contestSubmissionItemRepository.save(createSubmissionItem(
                emptyContest, null, "중간보고서", TODAY.atStartOfDay(), TODAY.plusDays(3).atTime(DEADLINE_TIME)));

        contestSubmissionDeadlineService.publishDeadlineNotifications(TODAY);

        assertThat(findEvents(submissionItem)).isEmpty();
    }

    private ContestSubmissionItem saveSubmissionItem(final ContestTrack track, final LocalDateTime endAt) {
        return contestSubmissionItemRepository.save(
                createSubmissionItem(contest, track, "중간보고서", endAt.minusDays(7), endAt));
    }

    private Member saveTeamMember(final Team team, final int uniqueNum) {
        final Member member = memberRepository.save(MemberFixture.createMemberWithUniqueNum(uniqueNum));
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀원))
                .build());
        return member;
    }

    private List<SubmissionDeadlineNotificationEvent> findEvents(final ContestSubmissionItem submissionItem) {
        return applicationEvents.stream(SubmissionDeadlineNotificationEvent.class)
                .filter(event -> event.submissionItemId().equals(submissionItem.getId()))
                .toList();
    }

    private SubmissionDeadlineNotificationEvent getOnlyEvent(final ContestSubmissionItem submissionItem) {
        final List<SubmissionDeadlineNotificationEvent> events = findEvents(submissionItem);
        assertThat(events).hasSize(1);
        return events.get(0);
    }
}
