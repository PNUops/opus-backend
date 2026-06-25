package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestFixture.createContest;
import static com.opus.opus.contest.ContestSubmissionFixture.createSubmission;
import static com.opus.opus.contest.ContestSubmissionItemFixture.createSubmissionItem;
import static com.opus.opus.contest.ContestTrackFixture.createTrack;
import static com.opus.opus.modules.contest.domain.SubmissionFileFormat.PDF;
import static com.opus.opus.modules.contest.domain.SubmissionFileFormat.ZIP;
import static com.opus.opus.modules.contest.domain.SubmissionVisibility.PRIVATE;
import static com.opus.opus.modules.contest.domain.SubmissionVisibility.PUBLIC;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.contest.exception.ContestTrackExceptionType.NOT_FOUND_TRACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionItemCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.contest.exception.ContestTrackException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ContestSubmissionItemCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionItemCommandService contestSubmissionItemCommandService;

    @Autowired
    private ContestSubmissionItemRepository contestSubmissionItemRepository;
    @Autowired
    private ContestSubmissionRepository contestSubmissionRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;

    private Contest contest;
    private ContestTrack track;
    private ContestSubmissionItemRequest request;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(createContest());
        track = contestTrackRepository.save(createTrack(contest));
        request = new ContestSubmissionItemRequest(
                "발표자료", track.getId(), "PDF 형식의 발표자료를 제출하세요.", List.of(PDF, ZIP),
                50, 3, LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 31, 23, 59), true, PUBLIC);
    }

    @Test
    @DisplayName("[성공] 분과가 지정된 제출 항목이 생성된다.")
    void 분과가_지정된_제출_항목이_생성된다() {
        contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request);

        final List<ContestSubmissionItem> submissionItems = contestSubmissionItemRepository.findAll();
        assertThat(submissionItems).hasSize(1);
        final ContestSubmissionItem submissionItem = submissionItems.get(0);
        assertThat(submissionItem.getName()).isEqualTo("발표자료");
        assertThat(submissionItem.getContest().getId()).isEqualTo(contest.getId());
        assertThat(submissionItem.getContestTrack().getId()).isEqualTo(track.getId());
        assertThat(submissionItem.getAllowedFileFormats()).containsExactlyInAnyOrder(PDF, ZIP);
        assertThat(submissionItem.getVisibility()).isEqualTo(PUBLIC);
    }

    @Test
    @DisplayName("[성공] 분과 ID가 없으면 전체 분과 대상 제출 항목이 생성된다.")
    void 분과_ID가_없으면_전체_분과_대상_제출_항목이_생성된다() {
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "발표자료", null, "PDF 형식의 발표자료를 제출하세요.", List.of(PDF, ZIP),
                50, 3, LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 31, 23, 59), true, PUBLIC);

        contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request);

        final List<ContestSubmissionItem> submissionItems = contestSubmissionItemRepository.findAll();
        assertThat(submissionItems).hasSize(1);
        assertThat(submissionItems.get(0).getContestTrack()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회면 제출 항목 생성에 실패한다.")
    void 존재하지_않는_대회면_제출_항목_생성에_실패한다() {
        assertThatThrownBy(() -> contestSubmissionItemCommandService.createSubmissionItem(999L, request))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 분과면 제출 항목 생성에 실패한다.")
    void 존재하지_않는_분과면_제출_항목_생성에_실패한다() {
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "발표자료", 999L, "PDF 형식의 발표자료를 제출하세요.", List.of(PDF, ZIP),
                50, 3, LocalDateTime.of(2026, 7, 1, 0, 0), LocalDateTime.of(2026, 7, 31, 23, 59), true, PUBLIC);

        assertThatThrownBy(() -> contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request))
                .isInstanceOf(ContestTrackException.class)
                .hasMessage(NOT_FOUND_TRACK.errorMessage());
    }

    @Test
    @DisplayName("[실패] 시작일시가 마감일시보다 이후면 제출 항목 생성에 실패한다.")
    void 시작일시가_마감일시보다_이후면_제출_항목_생성에_실패한다() {
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "발표자료", null, "PDF 형식의 발표자료를 제출하세요.", List.of(PDF, ZIP),
                50, 3, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 7, 1, 0, 0), true, PUBLIC);

        assertThatThrownBy(() -> contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_PERIOD.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출 항목의 설정값이 수정된다.")
    void 제출_항목의_설정값이_수정된다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "수정된 발표자료", track.getId(), "수정된 설명", List.of(PDF),
                100, 5, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59), false, PRIVATE);

        contestSubmissionItemCommandService.updateSubmissionItem(contest.getId(), submissionItem.getId(), request);

        final ContestSubmissionItem updated =
                contestSubmissionItemRepository.findById(submissionItem.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("수정된 발표자료");
        assertThat(updated.getMaxFileCount()).isEqualTo(5);
        assertThat(updated.getAllowedFileFormats()).containsExactly(PDF);
        assertThat(updated.getVisibility()).isEqualTo(PRIVATE);
    }

    @Test
    @DisplayName("[성공] 분과 ID가 없으면 전체 분과 대상으로 수정된다.")
    void 분과_ID가_없으면_전체_분과_대상으로_수정된다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "수정된 발표자료", null, "수정된 설명", List.of(PDF),
                100, 5, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59), false, PRIVATE);

        contestSubmissionItemCommandService.updateSubmissionItem(contest.getId(), submissionItem.getId(), request);

        final ContestSubmissionItem updated =
                contestSubmissionItemRepository.findById(submissionItem.getId()).orElseThrow();
        assertThat(updated.getContestTrack()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출 항목이면 수정에 실패한다.")
    void 존재하지_않는_제출_항목이면_수정에_실패한다() {
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "수정된 발표자료", track.getId(), "수정된 설명", List.of(PDF),
                100, 5, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59), false, PRIVATE);

        assertThatThrownBy(() -> contestSubmissionItemCommandService.updateSubmissionItem(contest.getId(), 999L, request))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(NOT_FOUND_SUBMISSION_ITEM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 대회의 제출 항목이면 수정에 실패한다.")
    void 다른_대회의_제출_항목이면_수정에_실패한다() {
        final Contest otherContest = contestRepository.save(createContest());
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(otherContest, null));
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "수정된 발표자료", null, "수정된 설명", List.of(PDF),
                100, 5, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59), false, PRIVATE);

        assertThatThrownBy(() -> contestSubmissionItemCommandService.updateSubmissionItem(
                contest.getId(), submissionItem.getId(), request))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_ITEM_FOR_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 시작일시가 마감일시보다 이후면 수정에 실패한다.")
    void 시작일시가_마감일시보다_이후면_수정에_실패한다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));
        final ContestSubmissionItemRequest request = new ContestSubmissionItemRequest(
                "수정된 발표자료", track.getId(), "수정된 설명", List.of(PDF),
                100, 5, LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 7, 1, 0, 0), false, PRIVATE);

        assertThatThrownBy(() -> contestSubmissionItemCommandService.updateSubmissionItem(
                contest.getId(), submissionItem.getId(), request))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_PERIOD.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출 항목과 해당 항목의 제출물이 함께 삭제된다.")
    void 제출_항목과_해당_항목의_제출물이_함께_삭제된다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));
        contestSubmissionRepository.save(createSubmission(1L, submissionItem));
        contestSubmissionRepository.save(createSubmission(2L, submissionItem));

        contestSubmissionItemCommandService.deleteSubmissionItem(contest.getId(), submissionItem.getId());

        assertThat(contestSubmissionItemRepository.findAll()).isEmpty();
        assertThat(contestSubmissionRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출 항목이면 삭제에 실패한다.")
    void 존재하지_않는_제출_항목이면_삭제에_실패한다() {
        assertThatThrownBy(() -> contestSubmissionItemCommandService.deleteSubmissionItem(contest.getId(), 999L))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(NOT_FOUND_SUBMISSION_ITEM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 대회의 제출 항목이면 삭제에 실패한다.")
    void 다른_대회의_제출_항목이면_삭제에_실패한다() {
        final Contest otherContest = contestRepository.save(createContest());
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(otherContest, null));

        assertThatThrownBy(() -> contestSubmissionItemCommandService.deleteSubmissionItem(
                contest.getId(), submissionItem.getId()))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_ITEM_FOR_CONTEST.errorMessage());
    }
}
