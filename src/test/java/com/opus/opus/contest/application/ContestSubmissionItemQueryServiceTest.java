package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestFixture.createContest;
import static com.opus.opus.contest.ContestSubmissionItemFixture.createSubmissionItem;
import static com.opus.opus.contest.ContestTrackFixture.createTrack;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestSubmissionItemQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ContestSubmissionItemQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionItemQueryService contestSubmissionItemQueryService;

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
        contest = contestRepository.save(createContest());
        track = contestTrackRepository.save(createTrack(contest));
    }

    @Test
    @DisplayName("[성공] 제출 항목의 설정값을 조회한다.")
    void 제출_항목의_설정값을_조회한다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));

        final ContestSubmissionItemResponse response =
                contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), submissionItem.getId());

        assertThat(response.name()).isEqualTo("발표자료");
        assertThat(response.contestTrackId()).isEqualTo(track.getId());
        assertThat(response.allowedFileFormats()).containsExactlyInAnyOrder("PDF", "ZIP");
        assertThat(response.maxFileSizeMb()).isEqualTo(50);
        assertThat(response.maxFileCount()).isEqualTo(3);
        assertThat(response.allowLateSubmission()).isTrue();
        assertThat(response.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    @DisplayName("[성공] 전체 분과 제출 항목은 분과 ID가 null로 조회된다.")
    void 전체_분과_제출_항목은_분과_ID가_null로_조회된다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, null));

        final ContestSubmissionItemResponse response =
                contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), submissionItem.getId());

        assertThat(response.contestTrackId()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출 항목이면 조회에 실패한다.")
    void 존재하지_않는_제출_항목이면_조회에_실패한다() {
        assertThatThrownBy(() -> contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), 999L))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(NOT_FOUND_SUBMISSION_ITEM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 대회의 제출 항목이면 조회에 실패한다.")
    void 다른_대회의_제출_항목이면_조회에_실패한다() {
        final Contest otherContest = contestRepository.save(createContest());
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(otherContest, null));

        assertThatThrownBy(() -> contestSubmissionItemQueryService.getSubmissionItem(
                contest.getId(), submissionItem.getId()))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_ITEM_FOR_CONTEST.errorMessage());
    }
}
