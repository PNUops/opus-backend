package com.opus.opus.contest.application;

import static com.opus.opus.member.MemberFixture.createMemberWithUniqueNum;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.team.TeamFixture.createTeamWithContestId;
import static com.opus.opus.team.TeamVoteFixture.createTeamVote;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestQueryService contestQueryService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;

    private Contest contest;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(1L));
    }

    @Test
    @DisplayName("[성공] 투표 기간 조회 시 저장된 기간을 반환한다.")
    void 투표_기간_조회_시_저장된_기간을_반환한다() {
        LocalDateTime startAt = now().plusDays(2);
        LocalDateTime endAt = now().plusDays(7);
        contest.updateVotePeriod(startAt, endAt);

        VotePeriodResponse response = contestQueryService.getVotePeriod(contest.getId());

        assertThat(response.voteStartAt()).isEqualTo(startAt);
        assertThat(response.voteEndAt()).isEqualTo(endAt);
    }

    @Test
    @DisplayName("[성공] 최대 투표 개수를 조회할 수 있다.")
    void 최대_투표_개수를_조회할_수_있다() {
        final Integer maxVotesLimit = 5;
        contest.updateMaxVotesLimit(maxVotesLimit);

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

    @Test
    @DisplayName("[성공] 대회의 투표 로그를 최신순으로 정렬할 수 있다.")
    void 대회의_투표_로그를_최신순으로_정렬할_수_있다() {
        final Member member = memberRepository.save(createMemberWithUniqueNum(1));
        final Member otherMember = memberRepository.save(createMemberWithUniqueNum(2));
        final Team team = teamRepository.save(createTeamWithContestId(contest.getId()));
        final Team otherTeam = teamRepository.save(createTeamWithContestId(contest.getId()));

        teamVoteRepository.save(createTeamVote(team, member.getId(), true));
        teamVoteRepository.save(createTeamVote(otherTeam, member.getId(), true));
        teamVoteRepository.save(createTeamVote(team, otherMember.getId(), true));

        final List<ContestVoteLogResponse> contestVoteLogResponses = contestQueryService.getContestVoteLog(
                contest.getId());

        assertThat(contestVoteLogResponses.size()).isEqualTo(3);
        assertThat(contestVoteLogResponses.get(0).votedAt()).isAfter(contestVoteLogResponses.get(1).votedAt());
    }
}
