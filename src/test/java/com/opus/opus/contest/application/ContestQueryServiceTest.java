package com.opus.opus.contest.application;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ContestQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestQueryService contestQueryService;

    @Autowired
    private ContestRepository contestRepository;

    private Contest contest;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest(1L));
    }

    @Test
    @DisplayName("[성공] 최대 투표 개수를 조회할 수 있다.")
    void 최대_투표_개수를_조회할_수_있다() {
        final Integer maxVotesLimit = 5;
        contest.updateMaxVotesLimit(maxVotesLimit);
        contestRepository.save(contest);

        final ContestVotesLimitResponse response = contestQueryService.getMaxVotesLimit(contest.getId());

        assertThat(response.maxVotesLimit()).isEqualTo(maxVotesLimit);
    }

    @Test
    @DisplayName("[성공] 기본 최대 투표 개수는 0이다.")
    void 기본_최대_투표_개수는_0이다() {
        final ContestVotesLimitResponse response = contestQueryService.getMaxVotesLimit(contest.getId());

        assertThat(response.maxVotesLimit()).isEqualTo(0);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 최대 투표 개수는 조회할 수 없다.")
    void 존재하지_않는_대회의_최대_투표_개수는_조회할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() -> {
            contestQueryService.getMaxVotesLimit(invalidContestId);
        }).isInstanceOf(ContestException.class).hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }
}
