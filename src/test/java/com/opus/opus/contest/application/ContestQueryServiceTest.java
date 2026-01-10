package com.opus.opus.contest.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestQueryService contestQueryService;

    @Autowired
    private ContestRepository contestRepository;

    private Contest contest;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
    }

    @Test
    @DisplayName("[성공] 투표 기간 조회 시 저장된 기간을 반환한다.")
    void 투표_기간_조회_시_저장된_기간을_반환한다() {
        LocalDateTime startAt = LocalDateTime.now().plusDays(2);
        LocalDateTime endAt = LocalDateTime.now().plusDays(7);
        contest.updateVotePeriod(startAt, endAt);

        VotePeriodResponse response = contestQueryService.getVotePeriod(contest.getId());

        assertThat(response.voteStartAt()).isEqualTo(startAt);
        assertThat(response.voteEndAt()).isEqualTo(endAt);
    }
}
