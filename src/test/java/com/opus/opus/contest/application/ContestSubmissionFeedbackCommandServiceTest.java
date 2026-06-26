package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
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
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import java.util.List;
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
    private MemberRepository memberRepository;

    private Contest contest;
    private ContestSubmission submission;
    private Member member;

    private final String description = "발표 흐름이 좋네요.";
    private final String updatedDescription = "데모 영상 길이를 줄여보세요.";

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        final ContestSubmissionItem submissionItem =
                submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(1L, submissionItem));
        member = memberRepository.save(MemberFixture.createMember());
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
                .isInstanceOf(ContestSubmissionException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }

}
