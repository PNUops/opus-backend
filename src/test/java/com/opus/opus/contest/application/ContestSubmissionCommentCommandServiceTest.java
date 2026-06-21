package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.COMMENT_NOT_BELONG_TO_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOTHING_TO_UPDATE;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOT_FOUND_COMMENT;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOT_OWNER_COMMENT;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionCommentCommandService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionComment;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionCommentRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionCommentException;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestSubmissionCommentCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionCommentCommandService commentCommandService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestSubmissionItemRepository submissionItemRepository;
    @Autowired
    private ContestSubmissionRepository submissionRepository;
    @Autowired
    private ContestSubmissionCommentRepository commentRepository;
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
    @DisplayName("[성공] 제출물 코멘트가 정상적으로 등록된다.")
    void 제출물_코멘트가_정상적으로_등록된다() {
        commentCommandService.createComment(contest.getId(), submission.getId(), member.getId(), description, null);

        final ContestSubmissionComment savedComment =
                commentRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0);
        assertThat(savedComment.getDescription()).isEqualTo(description);
        assertThat(savedComment.getMemberId()).isEqualTo(member.getId());
        assertThat(savedComment.getSubmission().getId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회에는 코멘트를 등록할 수 없다.")
    void 존재하지_않는_대회에는_코멘트를_등록할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() ->
                commentCommandService.createComment(invalidContestId, submission.getId(), member.getId(), description, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물에는 코멘트를 등록할 수 없다.")
    void 존재하지_않는_제출물에는_코멘트를_등록할_수_없다() {
        final Long invalidSubmissionId = 999L;

        assertThatThrownBy(() ->
                commentCommandService.createComment(contest.getId(), invalidSubmissionId, member.getId(), description, null))
                .isInstanceOf(ContestSubmissionException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출물 코멘트의 본문이 정상적으로 수정된다.")
    void 제출물_코멘트의_본문이_정상적으로_수정된다() {
        commentCommandService.createComment(
                contest.getId(), submission.getId(), member.getId(), description, null);
        final Long commentId = commentRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0).getId();

        commentCommandService.updateComment(
                contest.getId(), submission.getId(), commentId, member.getId(), updatedDescription, null, null);

        final ContestSubmissionComment updated = commentRepository.findById(commentId).orElseThrow();
        assertThat(updated.getDescription()).isEqualTo(updatedDescription);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 코멘트는 수정할 수 없다.")
    void 존재하지_않는_코멘트는_수정할_수_없다() {
        final Long invalidCommentId = 999L;

        assertThatThrownBy(() ->
                commentCommandService.updateComment(contest.getId(), submission.getId(), invalidCommentId,
                        member.getId(), updatedDescription, null, null))
                .isInstanceOf(ContestSubmissionCommentException.class)
                .hasMessage(NOT_FOUND_COMMENT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 코멘트는 수정할 수 없다.")
    void 본인이_작성하지_않은_코멘트는_수정할_수_없다() {
        commentCommandService.createComment(
                contest.getId(), submission.getId(), member.getId(), description, null);
        final Long commentId = commentRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0).getId();
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));

        assertThatThrownBy(() ->
                commentCommandService.updateComment(contest.getId(), submission.getId(), commentId,
                        otherMember.getId(), updatedDescription, null, null))
                .isInstanceOf(ContestSubmissionCommentException.class)
                .hasMessage(NOT_OWNER_COMMENT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 제출물의 코멘트는 수정할 수 없다.")
    void 다른_제출물의_코멘트는_수정할_수_없다() {
        commentCommandService.createComment(
                contest.getId(), submission.getId(), member.getId(), description, null);
        final Long commentId = commentRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0).getId();
        final ContestSubmission otherSubmission = submissionRepository.save(
                ContestSubmissionFixture.createSubmission(2L, submission.getSubmissionItem()));

        assertThatThrownBy(() ->
                commentCommandService.updateComment(contest.getId(), otherSubmission.getId(), commentId,
                        member.getId(), updatedDescription, null, null))
                .isInstanceOf(ContestSubmissionCommentException.class)
                .hasMessage(COMMENT_NOT_BELONG_TO_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 본문과 파일이 모두 비어있으면 수정할 수 없다.")
    void 본문과_파일이_모두_비어있으면_수정할_수_없다() {
        commentCommandService.createComment(
                contest.getId(), submission.getId(), member.getId(), description, null);
        final Long commentId = commentRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0).getId();

        assertThatThrownBy(() ->
                commentCommandService.updateComment(contest.getId(), submission.getId(), commentId,
                        member.getId(), null, null, null))
                .isInstanceOf(ContestSubmissionCommentException.class)
                .hasMessage(NOTHING_TO_UPDATE.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출물 코멘트가 정상적으로 삭제된다.")
    void 제출물_코멘트가_정상적으로_삭제된다() {
        commentCommandService.createComment(
                contest.getId(), submission.getId(), member.getId(), description, null);
        final Long commentId = commentRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0).getId();

        commentCommandService.deleteComment(contest.getId(), submission.getId(), commentId, member.getId());

        assertThat(commentRepository.findById(commentId)).isEmpty();
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 코멘트는 삭제할 수 없다.")
    void 본인이_작성하지_않은_코멘트는_삭제할_수_없다() {
        commentCommandService.createComment(
                contest.getId(), submission.getId(), member.getId(), description, null);
        final Long commentId = commentRepository.findAllBySubmissionIdOrderByIdDesc(submission.getId()).get(0).getId();
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));

        assertThatThrownBy(() ->
                commentCommandService.deleteComment(contest.getId(), submission.getId(), commentId, otherMember.getId()))
                .isInstanceOf(ContestSubmissionCommentException.class)
                .hasMessage(NOT_OWNER_COMMENT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 코멘트는 삭제할 수 없다.")
    void 존재하지_않는_코멘트는_삭제할_수_없다() {
        final Long invalidCommentId = 999L;

        assertThatThrownBy(() ->
                commentCommandService.deleteComment(contest.getId(), submission.getId(), invalidCommentId, member.getId()))
                .isInstanceOf(ContestSubmissionCommentException.class)
                .hasMessage(NOT_FOUND_COMMENT.errorMessage());
    }
}
