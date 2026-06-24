package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionFileQueryService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.exception.FileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class ContestSubmissionFileQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionFileQueryService contestSubmissionFileQueryService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestSubmissionItemRepository submissionItemRepository;
    @Autowired
    private ContestSubmissionRepository submissionRepository;

    @MockitoBean
    private FileDocumentQueryService fileDocumentQueryService;

    private Contest contest;
    private ContestSubmission submission;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        final ContestSubmissionItem item = submissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(1L, item));
    }

    @Test
    @DisplayName("[성공] 제출 파일을 개별 다운로드한다.")
    void 제출_파일을_개별_다운로드한다() {
        final Long fileId = 101L;
        when(fileDocumentQueryService.download(eq(fileId))).thenReturn(new DocumentFileDownload(
                new ByteArrayResource("content".getBytes()), "발표자료.pdf", "application/pdf", 1048576L,
                submission.getId()));

        final DocumentFileDownload result = contestSubmissionFileQueryService.downloadSubmissionFile(
                contest.getId(), submission.getId(), fileId);

        assertThat(result.fileName()).isEqualTo("발표자료.pdf");
        assertThat(result.fileSize()).isEqualTo(1048576L);
    }

    @Test
    @DisplayName("[실패] 파일이 해당 제출물에 속하지 않으면 예외가 발생한다.")
    void 파일이_해당_제출물에_속하지_않으면_예외가_발생한다() {
        final Long fileId = 101L;
        when(fileDocumentQueryService.download(eq(fileId))).thenReturn(new DocumentFileDownload(
                new ByteArrayResource("content".getBytes()), "발표자료.pdf", "application/pdf", 1048576L,
                99999L));

        assertThatThrownBy(() -> contestSubmissionFileQueryService.downloadSubmissionFile(
                contest.getId(), submission.getId(), fileId))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_FOUND.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물의 파일은 다운로드할 수 없다.")
    void 존재하지_않는_제출물의_파일은_다운로드할_수_없다() {
        assertThatThrownBy(() -> contestSubmissionFileQueryService.downloadSubmissionFile(
                contest.getId(), 99999L, 101L))
                .isInstanceOf(ContestSubmissionException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 파일은 다운로드할 수 없다.")
    void 존재하지_않는_대회의_파일은_다운로드할_수_없다() {
        assertThatThrownBy(() -> contestSubmissionFileQueryService.downloadSubmissionFile(
                99999L, submission.getId(), 101L))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }
}
