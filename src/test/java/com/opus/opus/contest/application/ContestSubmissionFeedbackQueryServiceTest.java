package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionFeedbackExceptionType.NOT_FOUND_FEEDBACK;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TEAM_NOT_IN_CONTEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFeedbackFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionFeedbackQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFeedbackResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMyFeedbackResponse;
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
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileFeedback;
import com.opus.opus.modules.file.domain.dao.FileFeedbackRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.team.TeamFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestSubmissionFeedbackQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionFeedbackQueryService feedbackQueryService;

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
    private MemberRepository memberRepository;
    @Autowired
    private FileFeedbackRepository fileFeedbackRepository;

    private Contest contest;
    private Team team;
    private ContestSubmissionItem submissionItem;
    private ContestSubmission submission;
    private Member member;
    private Member admin;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        submissionItem = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));
        member = memberRepository.save(MemberFixture.createMemberWithRole("이지민", 0, ROLE_외부멘토));
        admin = memberRepository.save(MemberFixture.createMemberWithRole("관리자", 9, ROLE_관리자));
    }

    @Test
    @DisplayName("[성공] 팀의 제출 항목에 달린 피드백 목록을 조회할 수 있다.")
    void 팀의_제출_항목에_달린_피드백_목록을_조회할_수_있다() {
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        feedbackRepository.save(createFeedback(member));
        feedbackRepository.save(createFeedback(otherMember));

        final List<ContestSubmissionFeedbackResponse> response =
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), submissionItem.getId(), admin);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).description())
                .isEqualTo(ContestSubmissionFeedbackFixture.FEEDBACK_DESCRIPTION);
        final ContestSubmissionFeedbackResponse mentorFeedback = response.stream()
                .filter(it -> it.memberId().equals(member.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(mentorFeedback.memberName()).isEqualTo(member.getName());
        assertThat(mentorFeedback.memberRoleType()).isEqualTo(ROLE_외부멘토.name());
    }

    @Test
    @DisplayName("[성공] 제출물이 삭제돼도 남아 있는 피드백 목록을 조회할 수 있다.")
    void 제출물이_삭제돼도_남아_있는_피드백_목록을_조회할_수_있다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(createFeedback(member));
        submissionRepository.delete(submission);

        final List<ContestSubmissionFeedbackResponse> response =
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), submissionItem.getId(), admin);

        assertThat(response).extracting(ContestSubmissionFeedbackResponse::feedbackId)
                .containsExactly(feedback.getId());
    }

    @Test
    @DisplayName("[성공] 다른 팀의 피드백은 목록에 포함되지 않는다.")
    void 다른_팀의_피드백은_목록에_포함되지_않는다() {
        final Team otherTeam = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndTeamName(contest.getId(), "보안 2팀"));
        final ContestSubmissionFeedback feedback = feedbackRepository.save(createFeedback(member));
        feedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submissionItem, otherTeam.getId(), member.getId()));

        final List<ContestSubmissionFeedbackResponse> response =
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), submissionItem.getId(), admin);

        assertThat(response).extracting(ContestSubmissionFeedbackResponse::feedbackId)
                .containsExactly(feedback.getId());
    }

    @Test
    @DisplayName("[성공] 피드백이 없는 제출 항목은 빈 리스트를 반환한다.")
    void 피드백이_없는_제출_항목은_빈_리스트를_반환한다() {
        final List<ContestSubmissionFeedbackResponse> response =
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), submissionItem.getId(), admin);

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("[성공] 피드백 목록은 최신순으로 정렬되어 조회된다.")
    void 피드백_목록은_최신순으로_정렬되어_조회된다() {
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        final ContestSubmissionFeedback first = feedbackRepository.save(createFeedback(member));
        final ContestSubmissionFeedback second = feedbackRepository.save(createFeedback(otherMember));

        final List<ContestSubmissionFeedbackResponse> response =
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), submissionItem.getId(), admin);

        assertThat(response).extracting(ContestSubmissionFeedbackResponse::feedbackId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("[성공] 피드백에 첨부된 파일이 함께 조회된다.")
    void 피드백에_첨부된_파일이_함께_조회된다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(createFeedback(member));
        final FileFeedback fileFeedback = fileFeedbackRepository.save(FileFeedback.builder()
                .file(File.create("피드백.pdf", "files/2026-06-21/feedback.pdf", "application/pdf", 524288L))
                .feedbackId(feedback.getId())
                .fileOrder(0)
                .build());

        final List<ContestSubmissionFeedbackResponse> response =
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), submissionItem.getId(), admin);

        assertThat(response.get(0).files()).hasSize(1);
        assertThat(response.get(0).files().get(0).fileId()).isEqualTo(fileFeedback.getId());
        assertThat(response.get(0).files().get(0).fileName()).isEqualTo("피드백.pdf");
        assertThat(response.get(0).files().get(0).fileSize()).isEqualTo(524288L);
    }

    @Test
    @DisplayName("[성공] 작성자가 탈퇴한 피드백도 작성자 정보 없이 조회된다.")
    void 작성자가_탈퇴한_피드백도_작성자_정보_없이_조회된다() {
        final Member leaver = memberRepository.save(MemberFixture.createMemberWithUniqueNum(3));
        feedbackRepository.save(createFeedback(leaver));
        memberRepository.delete(leaver);

        final List<ContestSubmissionFeedbackResponse> response =
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), submissionItem.getId(), admin);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).memberId()).isEqualTo(leaver.getId());
        assertThat(response.get(0).memberName()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 피드백 목록은 조회할 수 없다.")
    void 존재하지_않는_대회의_피드백_목록은_조회할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() ->
                feedbackQueryService.getFeedbacks(invalidContestId, team.getId(), submissionItem.getId(), admin))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출 항목의 피드백 목록은 조회할 수 없다.")
    void 존재하지_않는_제출_항목의_피드백_목록은_조회할_수_없다() {
        final Long invalidSubmissionItemId = 999L;

        assertThatThrownBy(() ->
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), invalidSubmissionItemId, admin))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(NOT_FOUND_SUBMISSION_ITEM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 본인이 작성한 피드백 단건을 조회할 수 있다.")
    void 본인이_작성한_피드백_단건을_조회할_수_있다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(createFeedback(member));

        final ContestSubmissionMyFeedbackResponse response =
                feedbackQueryService.getFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId());

        assertThat(response.feedbackId()).isEqualTo(feedback.getId());
        assertThat(response.description()).isEqualTo(ContestSubmissionFeedbackFixture.FEEDBACK_DESCRIPTION);
    }

    @Test
    @DisplayName("[실패] 작성한 피드백이 없으면 단건 조회 시 예외가 발생한다.")
    void 작성한_피드백이_없으면_단건_조회_시_예외가_발생한다() {
        assertThatThrownBy(() ->
                feedbackQueryService.getFeedback(contest.getId(), team.getId(), submissionItem.getId(), member.getId()))
                .isInstanceOf(ContestSubmissionFeedbackException.class)
                .hasMessage(NOT_FOUND_FEEDBACK.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 피드백의 파일은 다운로드할 수 없다.")
    void 존재하지_않는_피드백의_파일은_다운로드할_수_없다() {
        final Long invalidFeedbackId = 999L;

        assertThatThrownBy(() -> feedbackQueryService.downloadFeedbackFile(
                contest.getId(), team.getId(), submissionItem.getId(), invalidFeedbackId, 1L, admin))
                .isInstanceOf(ContestSubmissionFeedbackException.class)
                .hasMessage(NOT_FOUND_FEEDBACK.errorMessage());
    }

    @Test
    @DisplayName("[실패] 피드백에 속하지 않은 파일은 다운로드할 수 없다.")
    void 피드백에_속하지_않은_파일은_다운로드할_수_없다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(createFeedback(member));
        final Long invalidFileId = 999L;

        assertThatThrownBy(() -> feedbackQueryService.downloadFeedbackFile(
                contest.getId(), team.getId(), submissionItem.getId(), feedback.getId(), invalidFileId, admin))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_FOUND.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 팀의 피드백 파일은 다운로드할 수 없다.")
    void 다른_팀의_피드백_파일은_다운로드할_수_없다() {
        final Team otherTeam = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndTeamName(contest.getId(), "보안 2팀"));
        final ContestSubmissionFeedback otherTeamFeedback = feedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submissionItem, otherTeam.getId(), member.getId()));

        assertThatThrownBy(() -> feedbackQueryService.downloadFeedbackFile(
                contest.getId(), team.getId(), submissionItem.getId(), otherTeamFeedback.getId(), 1L, admin))
                .isInstanceOf(ContestSubmissionFeedbackException.class)
                .hasMessage(NOT_FOUND_FEEDBACK.errorMessage());
    }

    @Test
    @DisplayName("[실패] 팀이 해당 대회 소속이 아니면 피드백 목록을 조회할 수 없다.")
    void 팀이_해당_대회_소속이_아니면_피드백_목록을_조회할_수_없다() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));

        assertThatThrownBy(() ->
                feedbackQueryService.getFeedbacks(otherContest.getId(), team.getId(), submissionItem.getId(), admin))
                .isInstanceOf(TeamException.class)
                .hasMessage(TEAM_NOT_IN_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 제출 항목이 해당 대회 소속이 아니면 피드백 목록을 조회할 수 없다.")
    void 제출_항목이_해당_대회_소속이_아니면_피드백_목록을_조회할_수_없다() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));
        final ContestSubmissionItem otherContestItem =
                submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(otherContest));

        assertThatThrownBy(() ->
                feedbackQueryService.getFeedbacks(contest.getId(), team.getId(), otherContestItem.getId(), admin))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_ITEM_FOR_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 대회의 제출 항목으로 첨부파일을 다운로드하면 INVALID_SUBMISSION_ITEM_FOR_CONTEST 예외가 발생한다.")
    void 다른_대회의_제출_항목으로_첨부파일을_다운로드하면_INVALID_SUBMISSION_ITEM_FOR_CONTEST_예외가_발생한다() {
        final ContestSubmissionFeedback feedback = feedbackRepository.save(createFeedback(member));
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));
        final ContestSubmissionItem otherContestItem =
                submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(otherContest));

        assertThatThrownBy(() -> feedbackQueryService.downloadFeedbackFile(
                contest.getId(), team.getId(), otherContestItem.getId(), feedback.getId(), 1L, admin))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_ITEM_FOR_CONTEST.errorMessage());
    }

    private ContestSubmissionFeedback createFeedback(final Member writer) {
        return ContestSubmissionFeedbackFixture.createFeedback(submissionItem, team.getId(), writer.getId());
    }
}
