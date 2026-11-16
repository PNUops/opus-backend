package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestMemberFixture.createContestMember;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_TO_VIEW_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_ASSIGNED_TEAM;
import static com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackExceptionType.NOT_FOUND_FEEDBACK;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TEAM_NOT_IN_CONTEST;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFeedbackFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionFeedbackCommandService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackException;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.notification.application.event.SubmissionFeedbackNotificationEvent;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.team.TeamFixture;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;

@RecordApplicationEvents
public class ContestSubmissionFeedbackCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionFeedbackCommandService feedbackCommandService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestMemberRepository contestMemberRepository;
    @Autowired
    private ContestSubmissionItemRepository submissionItemRepository;
    @Autowired
    private ContestSubmissionRepository submissionRepository;
    @Autowired
    private ContestSubmissionFeedbackRepository feedbackRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ApplicationEvents applicationEvents;

    private Contest contest;
    private Team team;
    private ContestSubmissionItem submissionItem;
    private ContestSubmission submission;
    private Member member;

    private final String description = "발표 흐름이 좋네요.";
    private final String updatedDescription = "데모 영상 길이를 줄여보세요.";

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        submissionItem = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));
        member = memberRepository.save(MemberFixture.createMember());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(ROLE_팀원))
                .build());
        contestMemberRepository.save(createContestMember(contest, member.getId(), List.of(team.getId())));
    }

    @Test
    @DisplayName("[성공] 본인 피드백이 없으면 새로 생성된다.")
    void 본인_피드백이_없으면_새로_생성된다() {
        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId(),
                description, null, null);

        final List<ContestSubmissionFeedback> feedbacks = findFeedbacks(submissionItem);
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getDescription()).isEqualTo(description);
        assertThat(feedbacks.get(0).getMemberId()).isEqualTo(member.getId());
        assertThat(feedbacks.get(0).getTeamId()).isEqualTo(team.getId());
        assertThat(feedbacks.get(0).getSubmissionItem().getId()).isEqualTo(submissionItem.getId());
    }

    @Test
    @DisplayName("[성공] 본인 피드백이 이미 있으면 새 행을 만들지 않고 같은 행을 수정한다.")
    void 본인_피드백이_이미_있으면_같은_행을_수정한다() {
        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId(),
                description, null, null);
        final Long feedbackId = findFeedbacks(submissionItem).get(0).getId();

        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId(),
                updatedDescription, null, null);

        final List<ContestSubmissionFeedback> feedbacks = findFeedbacks(submissionItem);
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getId()).isEqualTo(feedbackId);
        assertThat(feedbacks.get(0).getDescription()).isEqualTo(updatedDescription);
    }

    @Test
    @DisplayName("[성공] 제출물을 삭제하고 다시 제출한 뒤 피드백을 저장하면 기존 피드백을 수정한다.")
    void 제출물을_삭제하고_다시_제출한_뒤_피드백을_저장하면_기존_피드백을_수정한다() {
        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId(),
                description, null, null);
        final Long feedbackId = findFeedbacks(submissionItem).get(0).getId();
        submissionRepository.delete(submission);
        submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));

        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId(),
                updatedDescription, null, null);

        final List<ContestSubmissionFeedback> feedbacks = findFeedbacks(submissionItem);
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getId()).isEqualTo(feedbackId);
        assertThat(feedbacks.get(0).getDescription()).isEqualTo(updatedDescription);
        assertThat(applicationEvents.stream(SubmissionFeedbackNotificationEvent.class)).hasSize(1);
    }

    @Test
    @DisplayName("[성공] 같은 제출 항목에 서로 다른 멘토는 각자의 피드백을 가진다.")
    void 같은_제출_항목에_서로_다른_멘토는_각자의_피드백을_가진다() {
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        contestMemberRepository.save(createContestMember(contest, otherMember.getId(), List.of(team.getId())));

        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId(),
                description, null, null);
        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(),
                otherMember.getId(), updatedDescription, null, null);

        assertThat(findFeedbacks(submissionItem)).hasSize(2);
    }

    @Test
    @DisplayName("[성공] 피드백을 처음 작성하면 팀원 전원에게 새 피드백 알림 이벤트를 발행한다.")
    void 피드백을_처음_작성하면_팀원_전원에게_새_피드백_알림_이벤트를_발행한다() {
        final Member teammate = memberRepository.save(MemberFixture.createMemberWithUniqueNum(2));
        teamMemberRepository.save(TeamMember.builder()
                .memberId(teammate.getId())
                .team(team)
                .roles(Set.of(ROLE_팀원))
                .build());
        final Member mentor = memberRepository.save(MemberFixture.createMemberWithUniqueNum(3));
        contestMemberRepository.save(createContestMember(contest, mentor.getId(), List.of(team.getId())));

        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), mentor.getId(),
                description, null, null);

        final List<SubmissionFeedbackNotificationEvent> events =
                applicationEvents.stream(SubmissionFeedbackNotificationEvent.class).toList();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).memberIds()).containsExactlyInAnyOrder(member.getId(), teammate.getId());
        assertThat(events.get(0).contestId()).isEqualTo(contest.getId());
        assertThat(events.get(0).teamId()).isEqualTo(team.getId());
        assertThat(events.get(0).submissionId()).isEqualTo(submission.getId());
        assertThat(events.get(0).submissionItemId()).isEqualTo(submissionItem.getId());
        assertThat(events.get(0).submissionItemName()).isEqualTo(submissionItem.getName());
    }

    @Test
    @DisplayName("[성공] 기존 피드백을 수정하면 새 피드백 알림 이벤트를 발행하지 않는다.")
    void 기존_피드백을_수정하면_새_피드백_알림_이벤트를_발행하지_않는다() {
        final Member mentor = memberRepository.save(MemberFixture.createMemberWithUniqueNum(3));
        contestMemberRepository.save(createContestMember(contest, mentor.getId(), List.of(team.getId())));
        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), mentor.getId(),
                description, null, null);

        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), mentor.getId(),
                updatedDescription, null, null);

        assertThat(applicationEvents.stream(SubmissionFeedbackNotificationEvent.class)).hasSize(1);
    }

    @Test
    @DisplayName("[성공] 다른 멘토가 같은 제출 항목에 처음 작성하면 새 피드백 알림 이벤트를 다시 발행한다.")
    void 다른_멘토가_같은_제출_항목에_처음_작성하면_새_피드백_알림_이벤트를_다시_발행한다() {
        final Member mentor = memberRepository.save(MemberFixture.createMemberWithUniqueNum(3));
        final Member otherMentor = memberRepository.save(MemberFixture.createMemberWithUniqueNum(4));
        contestMemberRepository.save(createContestMember(contest, mentor.getId(), List.of(team.getId())));
        contestMemberRepository.save(createContestMember(contest, otherMentor.getId(), List.of(team.getId())));

        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(), mentor.getId(),
                description, null, null);
        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(),
                otherMentor.getId(), updatedDescription, null, null);

        assertThat(applicationEvents.stream(SubmissionFeedbackNotificationEvent.class)).hasSize(2);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회에는 피드백을 저장할 수 없다.")
    void 존재하지_않는_대회에는_피드백을_저장할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(invalidContestId, team.getId(), submissionItem.getId(),
                        member.getId(), description, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 팀이 해당 대회 소속이 아니면 피드백을 저장할 수 없다.")
    void 팀이_해당_대회_소속이_아니면_피드백을_저장할_수_없다() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(otherContest.getId(), team.getId(), submissionItem.getId(),
                        member.getId(), description, null, null))
                .isInstanceOf(TeamException.class)
                .hasMessage(TEAM_NOT_IN_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 제출 항목이 해당 대회 소속이 아니면 피드백을 저장할 수 없다.")
    void 제출_항목이_해당_대회_소속이_아니면_피드백을_저장할_수_없다() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));
        final ContestSubmissionItem otherContestItem =
                submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(otherContest));

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(contest.getId(), team.getId(), otherContestItem.getId(),
                        member.getId(), description, null, null))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_ITEM_FOR_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 팀의 제출물이 없으면 피드백을 저장할 수 없다.")
    void 팀의_제출물이_없으면_피드백을_저장할_수_없다() {
        submissionRepository.delete(submission);

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(),
                        member.getId(), description, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
        assertThat(findFeedbacks(submissionItem)).isEmpty();
        assertThat(applicationEvents.stream(SubmissionFeedbackNotificationEvent.class)).isEmpty();
    }

    @Test
    @DisplayName("[실패] 담당 팀이 배정되지 않은 관계자는 피드백을 저장할 수 없다.")
    void 담당_팀이_배정되지_않은_관계자는_피드백을_저장할_수_없다() {
        final Member unassignedMentor = memberRepository.save(MemberFixture.createMemberWithUniqueNum(5));

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(),
                        unassignedMentor.getId(), description, null, null))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(NOT_ASSIGNED_TEAM.errorMessage());
        assertThat(findFeedbacks(submissionItem)).isEmpty();
        assertThat(applicationEvents.stream(SubmissionFeedbackNotificationEvent.class)).isEmpty();
    }

    @Test
    @DisplayName("[실패] 다른 팀을 담당하는 관계자는 피드백을 저장할 수 없다.")
    void 다른_팀을_담당하는_관계자는_피드백을_저장할_수_없다() {
        final Member otherMentor = memberRepository.save(MemberFixture.createMemberWithUniqueNum(6));
        final Team otherTeam = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndTeamName(contest.getId(), "보안 2팀"));
        contestMemberRepository.save(createContestMember(contest, otherMentor.getId(), List.of(otherTeam.getId())));

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(contest.getId(), team.getId(), submissionItem.getId(),
                        otherMentor.getId(), description, null, null))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(NOT_ASSIGNED_TEAM.errorMessage());
        assertThat(findFeedbacks(submissionItem)).isEmpty();
        assertThat(applicationEvents.stream(SubmissionFeedbackNotificationEvent.class)).isEmpty();
    }

    @Test
    @DisplayName("[실패] 팀에게만 공개된 제출물에는 피드백을 저장할 수 없다.")
    void 팀에게만_공개된_제출물에는_피드백을_저장할_수_없다() {
        final ContestSubmissionItem teamOnlyItem = submissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithVisibility(contest, SubmissionVisibility.TEAM));
        submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), teamOnlyItem));

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(contest.getId(), team.getId(), teamOnlyItem.getId(),
                        member.getId(), description, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_TO_VIEW_SUBMISSION.errorMessage());
        assertThat(findFeedbacks(teamOnlyItem)).isEmpty();
        assertThat(applicationEvents.stream(SubmissionFeedbackNotificationEvent.class)).isEmpty();
    }

    @Test
    @DisplayName("[성공] 관계자에게 공개된 제출물에는 피드백을 저장할 수 있다.")
    void 관계자에게_공개된_제출물에는_피드백을_저장할_수_있다() {
        final ContestSubmissionItem staffOnlyItem = submissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithVisibility(contest, SubmissionVisibility.STAFF));
        submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), staffOnlyItem));

        feedbackCommandService.saveFeedback(contest.getId(), team.getId(), staffOnlyItem.getId(), member.getId(),
                description, null, null);

        final List<ContestSubmissionFeedback> feedbacks = findFeedbacks(staffOnlyItem);
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getDescription()).isEqualTo(description);
        assertThat(feedbacks.get(0).getMemberId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("[성공] 팀원이 피드백을 읽음 처리하면 isRead가 true가 된다.")
    void 팀원이_피드백을_읽음_처리하면_isRead가_true가_된다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submissionItem, team.getId(), member.getId()));
        assertThat(feedback.getIsRead()).isFalse(); // 변경 전 값 검증

        feedbackCommandService.markFeedbackAsRead(contest.getId(), team.getId(), submissionItem.getId(),
                feedback.getId(), member);

        final ContestSubmissionFeedback updated = feedbackRepository.findById(feedback.getId()).orElseThrow();
        assertThat(updated.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("[성공] 제출물이 삭제돼도 남아 있는 피드백을 읽음 처리할 수 있다.")
    void 제출물이_삭제돼도_남아_있는_피드백을_읽음_처리할_수_있다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submissionItem, team.getId(), member.getId()));
        submissionRepository.delete(submission);
        assertThat(feedback.getIsRead()).isFalse(); // 변경 전 값 검증

        feedbackCommandService.markFeedbackAsRead(contest.getId(), team.getId(), submissionItem.getId(),
                feedback.getId(), member);

        final ContestSubmissionFeedback updated = feedbackRepository.findById(feedback.getId()).orElseThrow();
        assertThat(updated.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("[실패] 팀원이 아닌 사용자가 읽음 처리하면 TEAM_MEMBER_NOT_FOUND_IN_TEAM 예외가 발생한다.")
    void 팀원이_아닌_사용자가_읽음_처리하면_예외가_발생한다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submissionItem, team.getId(), member.getId()));
        final Member outsider = memberRepository.save(MemberFixture.createMemberWithUniqueNum(9));

        assertThatThrownBy(() ->
                feedbackCommandService.markFeedbackAsRead(contest.getId(), team.getId(), submissionItem.getId(),
                        feedback.getId(), outsider))
                .isInstanceOf(TeamMemberException.class)
                .satisfies(e -> assertThat(((TeamMemberException) e).exceptionType())
                        .isEqualTo(TEAM_MEMBER_NOT_FOUND_IN_TEAM));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 피드백을 읽음 처리하면 NOT_FOUND_FEEDBACK 예외가 발생한다.")
    void 존재하지_않는_피드백을_읽음_처리하면_예외가_발생한다() {
        final Long invalidFeedbackId = 999L;

        assertThatThrownBy(() ->
                feedbackCommandService.markFeedbackAsRead(contest.getId(), team.getId(), submissionItem.getId(),
                        invalidFeedbackId, member))
                .isInstanceOf(ContestSubmissionFeedbackException.class)
                .satisfies(e -> assertThat(((ContestSubmissionFeedbackException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_FEEDBACK));
    }

    @Test
    @DisplayName("[실패] 다른 팀의 피드백을 읽음 처리하면 NOT_FOUND_FEEDBACK 예외가 발생한다.")
    void 다른_팀의_피드백을_읽음_처리하면_NOT_FOUND_FEEDBACK_예외가_발생한다() {
        final Team otherTeam = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndTeamName(contest.getId(), "보안 2팀"));
        final ContestSubmissionFeedback otherTeamFeedback = feedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submissionItem, otherTeam.getId(), member.getId()));

        assertThatThrownBy(() ->
                feedbackCommandService.markFeedbackAsRead(contest.getId(), team.getId(), submissionItem.getId(),
                        otherTeamFeedback.getId(), member))
                .isInstanceOf(ContestSubmissionFeedbackException.class)
                .satisfies(e -> assertThat(((ContestSubmissionFeedbackException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_FEEDBACK));
        assertThat(feedbackRepository.findById(otherTeamFeedback.getId()).orElseThrow().getIsRead()).isFalse();
    }

    @Test
    @DisplayName("[실패] 대회에 속하지 않은 팀으로 읽음 처리하면 TEAM_NOT_IN_CONTEST 예외가 발생한다.")
    void 대회에_속하지_않은_팀으로_읽음_처리하면_TEAM_NOT_IN_CONTEST_예외가_발생한다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submissionItem, team.getId(), member.getId()));
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));
        final Team otherContestTeam = teamRepository.save(TeamFixture.createTeamWithContestId(otherContest.getId()));

        assertThatThrownBy(() ->
                feedbackCommandService.markFeedbackAsRead(contest.getId(), otherContestTeam.getId(),
                        submissionItem.getId(), feedback.getId(), member))
                .isInstanceOf(TeamException.class)
                .hasMessage(TEAM_NOT_IN_CONTEST.errorMessage());
        assertThat(feedbackRepository.findById(feedback.getId()).orElseThrow().getIsRead()).isFalse();
    }

    private List<ContestSubmissionFeedback> findFeedbacks(final ContestSubmissionItem item) {
        return feedbackRepository.findAllBySubmissionItemIdAndTeamIdOrderByIdDesc(item.getId(), team.getId());
    }
}
