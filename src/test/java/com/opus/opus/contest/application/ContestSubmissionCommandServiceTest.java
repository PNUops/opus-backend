package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_FILE_FORMAT;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_FILE_COUNT_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_FILE_SIZE_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_PERIOD_ENDED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionCommandService;
import com.opus.opus.modules.contest.application.dto.response.SubmissionCreateResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.modules.team.exception.TeamMemberExceptionType;
import com.opus.opus.team.TeamFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

public class ContestSubmissionCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionCommandService contestSubmissionCommandService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private ContestSubmissionItemRepository contestSubmissionItemRepository;
    @Autowired
    private ContestSubmissionRepository contestSubmissionRepository;
    @Autowired
    private FileDocumentRepository fileDocumentRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Contest contest;
    private Team team;
    private Member member;
    private ContestSubmissionItem submissionItem;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀장))
                .build());
        submissionItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));
    }

    private MockMultipartFile pdf(final String filename) {
        return new MockMultipartFile("files", filename, "application/pdf", "content".getBytes());
    }

    private FileDocument saveFileDocument(final Long submissionId, final int fileOrder, final String name) {
        final File file = File.create(name, "files/2026-06-24/" + name, "application/pdf", 1024L);
        return fileDocumentRepository.save(FileDocument.builder()
                .file(file)
                .submissionId(submissionId)
                .fileOrder(fileOrder)
                .build());
    }

    @Test
    @DisplayName("[성공] 팀원이 제출 항목에 파일을 제출하면 제출이 생성된다.")
    void 제출을_생성한다() {
        final List<MockMultipartFile> files = List.of(pdf("발표자료.pdf"), pdf("데모영상.pdf"));

        final SubmissionCreateResponse response = contestSubmissionCommandService.createSubmission(
                contest.getId(), submissionItem.getId(), team.getId(), List.copyOf(files), member);

        assertThat(response.submissionId()).isNotNull();
        assertThat(contestSubmissionRepository.findById(response.submissionId())).isPresent();
        verify(fileDocumentCommandService, times(2))
                .storeDocumentFile(any(), eq(response.submissionId()), any());
    }

    @Test
    @DisplayName("[실패] 이미 제출한 항목에 다시 제출하면 예외가 발생한다.")
    void 중복_제출_시_예외() {
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));

        assertThatThrownBy(() -> contestSubmissionCommandService.createSubmission(
                contest.getId(), submissionItem.getId(), team.getId(), List.of(pdf("발표자료.pdf")), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(SUBMISSION_ALREADY_EXISTS.errorMessage());
    }

    @Test
    @DisplayName("[실패] 허용되지 않는 확장자를 제출하면 예외가 발생한다.")
    void 허용되지_않는_확장자_예외() {
        assertThatThrownBy(() -> contestSubmissionCommandService.createSubmission(
                contest.getId(), submissionItem.getId(), team.getId(), List.of(pdf("악성코드.exe")), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(INVALID_SUBMISSION_FILE_FORMAT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 최대 파일 수를 초과하면 예외가 발생한다.")
    void 파일_수_초과_예외() {
        final ContestSubmissionItem limitedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithMaxFileCount(contest, 1));

        assertThatThrownBy(() -> contestSubmissionCommandService.createSubmission(
                contest.getId(), limitedItem.getId(), team.getId(),
                List.of(pdf("a.pdf"), pdf("b.pdf")), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(SUBMISSION_FILE_COUNT_EXCEEDED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 최대 파일 크기를 초과하면 예외가 발생한다.")
    void 파일_크기_초과_예외() {
        final ContestSubmissionItem smallItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithMaxFileSizeMb(contest, 1));
        final MockMultipartFile bigFile = new MockMultipartFile(
                "files", "big.pdf", "application/pdf", new byte[2 * 1024 * 1024]);

        assertThatThrownBy(() -> contestSubmissionCommandService.createSubmission(
                contest.getId(), smallItem.getId(), team.getId(), List.of(bigFile), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(SUBMISSION_FILE_SIZE_EXCEEDED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 마감 후 지각 제출이 허용되지 않으면 예외가 발생한다.")
    void 마감_후_지각_불가_예외() {
        final ContestSubmissionItem closedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(
                        contest, LocalDateTime.now().minusDays(1), false));

        assertThatThrownBy(() -> contestSubmissionCommandService.createSubmission(
                contest.getId(), closedItem.getId(), team.getId(), List.of(pdf("발표자료.pdf")), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(SUBMISSION_PERIOD_ENDED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 해당 팀 소속이 아닌 학생은 제출할 수 없다.")
    void 비소속_학생_제출_예외() {
        final Member other = memberRepository.save(MemberFixture.createMemberWithUniqueNum(2));

        assertThatThrownBy(() -> contestSubmissionCommandService.createSubmission(
                contest.getId(), submissionItem.getId(), team.getId(), List.of(pdf("발표자료.pdf")), other))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 기존 제출에 파일을 추가하면 기존 fileOrder 다음 번호로 저장된다.")
    void 파일을_추가한다() {
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));
        saveFileDocument(submission.getId(), 1, "기존1.pdf");
        saveFileDocument(submission.getId(), 2, "기존2.pdf");

        contestSubmissionCommandService.addFiles(
                contest.getId(), submission.getId(), List.of(pdf("추가.pdf")), member);

        verify(fileDocumentCommandService).storeDocumentFile(any(), eq(submission.getId()), eq(3));
    }

    @Test
    @DisplayName("[실패] 기존 파일을 포함해 최대 파일 수를 초과하면 파일 추가는 실패한다.")
    void 파일_추가_시_총_개수_초과_예외() {
        final ContestSubmissionItem limitedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithMaxFileCount(contest, 2));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), limitedItem));
        saveFileDocument(submission.getId(), 1, "기존1.pdf");
        saveFileDocument(submission.getId(), 2, "기존2.pdf");

        assertThatThrownBy(() -> contestSubmissionCommandService.addFiles(
                contest.getId(), submission.getId(), List.of(pdf("추가.pdf")), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(SUBMISSION_FILE_COUNT_EXCEEDED.errorMessage());
    }

    @Test
    @DisplayName("[성공] 파일이 여러 개일 때 한 건을 삭제하면 제출은 유지된다.")
    void 파일_한_건을_삭제한다() {
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));
        final FileDocument target = saveFileDocument(submission.getId(), 1, "삭제대상.pdf");
        saveFileDocument(submission.getId(), 2, "유지.pdf");

        contestSubmissionCommandService.deleteFile(contest.getId(), submission.getId(), target.getId(), member);

        verify(fileDocumentCommandService).deleteDocumentFile(target.getId());
        assertThat(contestSubmissionRepository.findById(submission.getId())).isPresent();
    }

    @Test
    @DisplayName("[성공] 마지막 파일을 삭제하면 제출 자체가 취소된다.")
    void 마지막_파일_삭제_시_제출_취소() {
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));
        final FileDocument target = saveFileDocument(submission.getId(), 1, "마지막.pdf");

        contestSubmissionCommandService.deleteFile(contest.getId(), submission.getId(), target.getId(), member);

        verify(fileDocumentCommandService).deleteDocumentFile(target.getId());
        assertThat(contestSubmissionRepository.findById(submission.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] 제출에 속하지 않는 파일을 삭제하면 예외가 발생한다.")
    void 존재하지_않는_파일_삭제_예외() {
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), submissionItem));
        saveFileDocument(submission.getId(), 1, "유일.pdf");

        assertThatThrownBy(() -> contestSubmissionCommandService.deleteFile(
                contest.getId(), submission.getId(), 99999L, member))
                .isInstanceOf(FileException.class)
                .hasMessage(FileExceptionType.NOT_FOUND.errorMessage());
    }
}
