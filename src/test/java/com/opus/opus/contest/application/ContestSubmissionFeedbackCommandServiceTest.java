package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackExceptionType.NOT_FOUND_FEEDBACK;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFeedbackFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionFeedbackCommandService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionFeedback;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.team.TeamFixture;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestSubmissionFeedbackCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionFeedbackCommandService feedbackCommandService;

    @Autowired
    private ContestRepository contestRepository;
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

    private Contest contest;
    private Team team;
    private ContestSubmission submission;
    private Member member;

    private final String description = "발표 흐름이 좋네요.";
    private final String updatedDescription = "데모 영상 길이를 줄여보세요.";

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        final ContestSubmissionItem submissionItem =
                submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));
        member = memberRepository.save(MemberFixture.createMember());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(ROLE_팀원))
                .build());
    }

    @Test
    @DisplayName("[성공] 본인 피드백이 없으면 새로 생성된다.")
    void 본인_피드백이_없으면_새로_생성된다() {
        feedbackCommandService.saveFeedback(contest.getId(), submission.getId(), member.getId(), description, null, null);

        final List<ContestSubmissionFeedback> feedbacks =
                feedbackRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId());
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getDescription()).isEqualTo(description);
        assertThat(feedbacks.get(0).getMemberId()).isEqualTo(member.getId());
        assertThat(feedbacks.get(0).getSubmission().getId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[성공] 본인 피드백이 이미 있으면 새 행을 만들지 않고 같은 행을 수정한다.")
    void 본인_피드백이_이미_있으면_같은_행을_수정한다() {
        feedbackCommandService.saveFeedback(contest.getId(), submission.getId(), member.getId(), description, null, null);
        final Long feedbackId =
                feedbackRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0).getId();

        feedbackCommandService.saveFeedback(
                contest.getId(), submission.getId(), member.getId(), updatedDescription, null, null);

        final List<ContestSubmissionFeedback> feedbacks =
                feedbackRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId());
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getId()).isEqualTo(feedbackId);
        assertThat(feedbacks.get(0).getDescription()).isEqualTo(updatedDescription);
    }

    @Test
    @DisplayName("[성공] 같은 제출물에 서로 다른 멘토는 각자의 피드백을 가진다.")
    void 같은_제출물에_서로_다른_멘토는_각자의_피드백을_가진다() {
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));

        feedbackCommandService.saveFeedback(contest.getId(), submission.getId(), member.getId(), description, null, null);
        feedbackCommandService.saveFeedback(
                contest.getId(), submission.getId(), otherMember.getId(), updatedDescription, null, null);

        assertThat(feedbackRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId())).hasSize(2);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회에는 피드백을 저장할 수 없다.")
    void 존재하지_않는_대회에는_피드백을_저장할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(invalidContestId, submission.getId(), member.getId(), description, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물에는 피드백을 저장할 수 없다.")
    void 존재하지_않는_제출물에는_피드백을_저장할_수_없다() {
        final Long invalidSubmissionId = 999L;

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(contest.getId(), invalidSubmissionId, member.getId(), description, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 제출물이 해당 대회 소속이 아니면 피드백을 저장할 수 없다.")
    void 제출물이_해당_대회_소속이_아니면_피드백을_저장할_수_없다() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));

        assertThatThrownBy(() ->
                feedbackCommandService.saveFeedback(otherContest.getId(), submission.getId(), member.getId(), description, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(INVALID_SUBMISSION_FOR_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 팀원이 피드백을 읽음 처리하면 isRead가 true가 된다.")
    void 팀원이_피드백을_읽음_처리하면_isRead가_true가_된다() {
        final ContestSubmissionFeedback feedback =
                feedbackRepository.save(ContestSubmissionFeedbackFixture.createFeedback(submission, member.getId()));

        feedbackCommandService.markFeedbackAsRead(contest.getId(), submission.getId(), feedback.getId(), team.getId(), member);

        final ContestSubmissionFeedback updated = feedbackRepository.findById(feedback.getId()).orElseThrow();
        assertThat(updated.getIsRead()).isTrue();
    }

    @Test
    @DisplayName("[실패] 팀원이 아닌 사용자가 읽음 처리하면 TEAM_MEMBER_NOT_FOUND_IN_TEAM 예외가 발생한다.")
    void 팀원이_아닌_사용자가_읽음_처리하면_예외가_발생한다() {
        final ContestSubmissionFeedback feedback =
                feedbackRepository.save(ContestSubmissionFeedbackFixture.createFeedback(submission, member.getId()));
        final Member outsider = memberRepository.save(MemberFixture.createMemberWithUniqueNum(9));

        assertThatThrownBy(() ->
                feedbackCommandService.markFeedbackAsRead(contest.getId(), submission.getId(), feedback.getId(), team.getId(), outsider))
                .isInstanceOf(TeamMemberException.class)
                .satisfies(e -> assertThat(((TeamMemberException) e).exceptionType())
                        .isEqualTo(TEAM_MEMBER_NOT_FOUND_IN_TEAM));
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 피드백을 읽음 처리하면 NOT_FOUND_FEEDBACK 예외가 발생한다.")
    void 존재하지_않는_피드백을_읽음_처리하면_예외가_발생한다() {
        final Long invalidFeedbackId = 999L;

        assertThatThrownBy(() ->
                feedbackCommandService.markFeedbackAsRead(contest.getId(), submission.getId(), invalidFeedbackId, team.getId(), member))
                .isInstanceOf(ContestSubmissionFeedbackException.class)
                .satisfies(e -> assertThat(((ContestSubmissionFeedbackException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_FEEDBACK));
    }

}
