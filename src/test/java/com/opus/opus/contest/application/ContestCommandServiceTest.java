package com.opus.opus.contest.application;

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

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
    }

    @Test
    @DisplayName("[성공] 투표 기간 수정 시 시작일과 종료일이 정상적으로 업데이트된다.")
    void 투표_기간_수정_시_시작일과_종료일이_정상적으로_업데이트된다() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        LocalDateTime endAt = LocalDateTime.now().plusDays(5);
        VoteUpdateRequest request = new VoteUpdateRequest(startAt, endAt);

        contestCommandService.updateVotePeriod(contest.getId(), request);

        Contest updatedContest = contestRepository.findById(contest.getId()).orElseThrow();
        assertThat(updatedContest.getVoteStartAt()).isEqualTo(startAt);
        assertThat(updatedContest.getVoteEndAt()).isEqualTo(endAt);
    }

    @Test
    @DisplayName("[실패] 투표 종료일이 시작일보다 앞서면 예외가 발생한다.")
    void 투표_종료일이_시작일보다_앞서면_예외가_발생한다() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(5);
        LocalDateTime endAt = LocalDateTime.now().plusDays(1);
        VoteUpdateRequest request = new VoteUpdateRequest(startAt, endAt);

        assertThatThrownBy(() -> {contestCommandService.updateVotePeriod(contest.getId(), request);})
                .isInstanceOf(ContestException.class)
                .hasMessage(VOTE_END_PRECEDE_VOTE_START.errorMessage());
    }
}
