package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestTrackExceptionType.NOT_FOUND_TRACK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionItemCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
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
    private ContestRepository contestRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;

    private Contest contest;
    private ContestTrack track;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
    }

    @Test
    @DisplayName("[성공] 분과가 지정된 제출 항목이 생성된다.")
    void 분과가_지정된_제출_항목이_생성된다() {
        // given
        final ContestSubmissionItemRequest request = ContestSubmissionItemFixture.createRequest(track.getId());

        // when
        contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request);

        // then
        final List<ContestSubmissionItem> submissionItems = contestSubmissionItemRepository.findAll();
        assertThat(submissionItems).hasSize(1);
        final ContestSubmissionItem submissionItem = submissionItems.get(0);
        assertThat(submissionItem.getName()).isEqualTo("발표자료");
        assertThat(submissionItem.getContest().getId()).isEqualTo(contest.getId());
        assertThat(submissionItem.getContestTrack().getId()).isEqualTo(track.getId());
        assertThat(submissionItem.getAllowedFileFormats())
                .containsExactlyInAnyOrder(SubmissionFileFormat.PDF, SubmissionFileFormat.ZIP);
        assertThat(submissionItem.getVisibility()).isEqualTo(SubmissionVisibility.PUBLIC);
    }

    @Test
    @DisplayName("[성공] 분과 ID가 없으면 전체 분과 대상 제출 항목이 생성된다.")
    void 분과_ID가_없으면_전체_분과_대상_제출_항목이_생성된다() {
        // given
        final ContestSubmissionItemRequest request = ContestSubmissionItemFixture.createRequest(null);

        // when
        contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request);

        // then
        final List<ContestSubmissionItem> submissionItems = contestSubmissionItemRepository.findAll();
        assertThat(submissionItems).hasSize(1);
        assertThat(submissionItems.get(0).getContestTrack()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회면 제출 항목 생성에 실패한다.")
    void 존재하지_않는_대회면_제출_항목_생성에_실패한다() {
        // given
        final ContestSubmissionItemRequest request = ContestSubmissionItemFixture.createRequest(null);

        // when & then
        assertThatThrownBy(() -> contestSubmissionItemCommandService.createSubmissionItem(999L, request))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 분과면 제출 항목 생성에 실패한다.")
    void 존재하지_않는_분과면_제출_항목_생성에_실패한다() {
        // given
        final ContestSubmissionItemRequest request = ContestSubmissionItemFixture.createRequest(999L);

        // when & then
        assertThatThrownBy(() -> contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request))
                .isInstanceOf(ContestTrackException.class)
                .hasMessage(NOT_FOUND_TRACK.errorMessage());
    }

    @Test
    @DisplayName("[실패] 시작일시가 마감일시보다 이후면 제출 항목 생성에 실패한다.")
    void 시작일시가_마감일시보다_이후면_제출_항목_생성에_실패한다() {
        // given
        final ContestSubmissionItemRequest request = ContestSubmissionItemFixture.createRequestWithPeriod(
                LocalDateTime.of(2026, 8, 1, 0, 0),
                LocalDateTime.of(2026, 7, 1, 0, 0)
        );

        // when & then
        assertThatThrownBy(() -> contestSubmissionItemCommandService.createSubmissionItem(contest.getId(), request))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_PERIOD.errorMessage());
    }
}
