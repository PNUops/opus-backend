package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionCommentFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionCommentQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionCommentResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionComment;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionCommentRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileComment;
import com.opus.opus.modules.file.domain.dao.FileCommentRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestSubmissionCommentQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionCommentQueryService commentQueryService;

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
    @Autowired
    private FileCommentRepository fileCommentRepository;

    private Contest contest;
    private ContestSubmission submission;
    private Member member;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        final ContestSubmissionItem submissionItem =
                submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(1L, submissionItem));
        member = memberRepository.save(MemberFixture.createMember());
    }

    @Test
    @DisplayName("[성공] 제출물의 코멘트 목록을 조회할 수 있다.")
    void 제출물의_코멘트_목록을_조회할_수_있다() {
        commentRepository.save(ContestSubmissionCommentFixture.createComment(submission, member.getId()));
        commentRepository.save(ContestSubmissionCommentFixture.createComment(submission, member.getId()));

        final List<ContestSubmissionCommentResponse> response =
                commentQueryService.getComments(contest.getId(), submission.getId());

        assertThat(response).hasSize(2);
        assertThat(response.get(0).description())
                .isEqualTo(ContestSubmissionCommentFixture.COMMENT_DESCRIPTION);
        assertThat(response.get(0).memberId()).isEqualTo(member.getId());
        assertThat(response.get(0).memberName()).isEqualTo(member.getName());
    }

    @Test
    @DisplayName("[성공] 코멘트가 없는 제출물은 빈 리스트를 반환한다.")
    void 코멘트가_없는_제출물은_빈_리스트를_반환한다() {
        final List<ContestSubmissionCommentResponse> response =
                commentQueryService.getComments(contest.getId(), submission.getId());

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("[성공] 코멘트 목록은 최신순으로 정렬되어 조회된다.")
    void 코멘트_목록은_최신순으로_정렬되어_조회된다() {
        final ContestSubmissionComment first =
                commentRepository.save(ContestSubmissionCommentFixture.createComment(submission, member.getId()));
        final ContestSubmissionComment second =
                commentRepository.save(ContestSubmissionCommentFixture.createComment(submission, member.getId()));

        final List<ContestSubmissionCommentResponse> response =
                commentQueryService.getComments(contest.getId(), submission.getId());

        assertThat(response).extracting(ContestSubmissionCommentResponse::commentId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("[성공] 코멘트에 첨부된 파일이 함께 조회된다.")
    void 코멘트에_첨부된_파일이_함께_조회된다() {
        final ContestSubmissionComment comment =
                commentRepository.save(ContestSubmissionCommentFixture.createComment(submission, member.getId()));
        final FileComment fileComment = fileCommentRepository.save(FileComment.builder()
                .file(File.create("피드백.pdf", "files/2026-06-21/feedback.pdf", "application/pdf", 524288L))
                .commentId(comment.getId())
                .fileOrder(0)
                .build());

        final List<ContestSubmissionCommentResponse> response =
                commentQueryService.getComments(contest.getId(), submission.getId());

        assertThat(response.get(0).files()).hasSize(1);
        assertThat(response.get(0).files().get(0).fileId()).isEqualTo(fileComment.getId());
        assertThat(response.get(0).files().get(0).fileName()).isEqualTo("피드백.pdf");
        assertThat(response.get(0).files().get(0).fileSize()).isEqualTo(524288L);
    }

    @Test
    @DisplayName("[성공] 작성자가 탈퇴한 코멘트도 작성자 정보 없이 조회된다.")
    void 작성자가_탈퇴한_코멘트도_작성자_정보_없이_조회된다() {
        final Member leaver = memberRepository.save(MemberFixture.createMemberWithUniqueNum(3));
        commentRepository.save(ContestSubmissionCommentFixture.createComment(submission, leaver.getId()));
        memberRepository.delete(leaver);

        final List<ContestSubmissionCommentResponse> response =
                commentQueryService.getComments(contest.getId(), submission.getId());

        assertThat(response).hasSize(1);
        assertThat(response.get(0).memberId()).isEqualTo(leaver.getId());
        assertThat(response.get(0).memberName()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 코멘트 목록은 조회할 수 없다.")
    void 존재하지_않는_대회의_코멘트_목록은_조회할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() -> commentQueryService.getComments(invalidContestId, submission.getId()))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물의 코멘트 목록은 조회할 수 없다.")
    void 존재하지_않는_제출물의_코멘트_목록은_조회할_수_없다() {
        final Long invalidSubmissionId = 999L;

        assertThatThrownBy(() -> commentQueryService.getComments(contest.getId(), invalidSubmissionId))
                .isInstanceOf(ContestSubmissionException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }
}
