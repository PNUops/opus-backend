package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.VOTE_END_PRECEDE_VOTE_START;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestCommandService contestCommandService;

    @Autowired
    private ContestRepository contestRepository;

    private Contest contest;
    private static final Integer MAX_VOTES_LIMIT = 5;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));
    }

    @Test
    @DisplayName("[성공] 투표 기간 수정 시 시작일과 종료일이 정상적으로 업데이트된다.")
    void 투표_기간_수정_시_시작일과_종료일이_정상적으로_업데이트된다() {
        // given
        final LocalDateTime originalStartAt = contest.getVoteStartAt();
        final LocalDateTime originalEndAt = contest.getVoteEndAt();

        final LocalDateTime newStartAt = LocalDateTime.now().plusDays(1);
        final LocalDateTime newEndAt = LocalDateTime.now().plusDays(5);
        final VoteUpdateRequest request = new VoteUpdateRequest(newStartAt, newEndAt);

        // when
        contestCommandService.updateVotePeriod(contest.getId(), request);

        // then
        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow();
        assertThat(updatedContest.getVoteStartAt()).isNotEqualTo(originalStartAt);
        assertThat(updatedContest.getVoteEndAt()).isNotEqualTo(originalEndAt);

        assertThat(updatedContest.getVoteStartAt()).isEqualTo(newStartAt);
        assertThat(updatedContest.getVoteEndAt()).isEqualTo(newEndAt);
    }

    @Test
    @DisplayName("[실패] 투표 종료일이 시작일보다 앞서면 예외가 발생한다.")
    void 투표_종료일이_시작일보다_앞서면_예외가_발생한다() {
        final LocalDateTime startAt = LocalDateTime.now().plusDays(5);
        final LocalDateTime endAt = LocalDateTime.now().plusDays(1);
        final VoteUpdateRequest request = new VoteUpdateRequest(startAt, endAt);

        assertThatThrownBy(() -> {contestCommandService.updateVotePeriod(contest.getId(), request);})
                .isInstanceOf(ContestException.class)
                .hasMessage(VOTE_END_PRECEDE_VOTE_START.errorMessage());
    }

    @Test
    @DisplayName("[성공] 최대 투표 개수가 정상적으로 설정된다.")
    void 최대_투표_개수가_정상적으로_설정된다() {
        contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);

        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow();
        assertThat(updatedContest.getMaxVotesLimit()).isEqualTo(MAX_VOTES_LIMIT);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 최대 투표 개수는 설정할 수 없다.")
    void 존재하지_않는_대회의_최대_투표_개수는_설정할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() -> {
            contestCommandService.updateMaxVotesLimit(invalidContestId, MAX_VOTES_LIMIT);
        }).isInstanceOf(ContestException.class).hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 진행 중에는 최대 투표 개수를 변경할 수 있다.")
    void 투표_진행_중에는_최대_투표_개수를_변경할_수_있다() {
        final LocalDateTime now = LocalDateTime.now();
        contest.updateVotePeriod(now.minusDays(1), now.plusDays(1));

        assertThatThrownBy(() -> {
            contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);
        }).isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_DURING_VOTING_PERIOD.errorMessage());
    }

    @Test
    @DisplayName("[성공] 투표 시작 전에는 최대 투표 개수를 변경할 수 있다.")
    void 투표_시작_전에는_최대_투표_개수를_변경할_수_있다() {
        final LocalDateTime now = LocalDateTime.now();
        contest.updateVotePeriod(now.plusDays(1), now.plusDays(2));

        contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);

        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow();
        assertThat(updatedContest.getMaxVotesLimit()).isEqualTo(MAX_VOTES_LIMIT);
    }

    @Test
    @DisplayName("[성공] 투표 종료 후에는 최대 투표 개수를 변경할 수 있다.")
    void 투표_종료_후에는_최대_투표_개수를_변경할_수_있다() {
        final LocalDateTime now = LocalDateTime.now();
        contest.updateVotePeriod(now.minusDays(2), now.minusDays(1));
        assertThat(contest.getMaxVotesLimit()).isEqualTo(0); // 변경 전 값 검증

        contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);

        final Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow(); // 변경 후 값 검증
        assertThat(updatedContest.getMaxVotesLimit()).isEqualTo(MAX_VOTES_LIMIT);
    }
}
