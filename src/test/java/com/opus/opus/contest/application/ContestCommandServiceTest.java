package com.opus.opus.contest.application;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ContestCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestCommandService contestCommandService;

    @Autowired
    private ContestRepository contestRepository;

    private Contest contest;
    private static final Integer MAX_VOTES_LIMIT = 5;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest(1L));
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
    @DisplayName("[실패] 투표 진행 중에는 최대 투표 개수를 변경할 수 없다.")
    void 투표_진행_중에는_최대_투표_개수를_변경할_수_없다() {
        final LocalDateTime now = LocalDateTime.now();
        contest.updateVotePeriod(now.minusDays(1), now.plusDays(1));

        assertThatThrownBy(() -> {
            contestCommandService.updateMaxVotesLimit(contest.getId(), MAX_VOTES_LIMIT);
        }).isInstanceOf(ContestException.class)
                .hasMessage(CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD.errorMessage());
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
