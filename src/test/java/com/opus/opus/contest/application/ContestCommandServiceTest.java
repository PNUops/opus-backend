package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestFixture.createContest;
import static com.opus.opus.contest.ContestSortFixture.createContestSort;
import static com.opus.opus.modules.contest.domain.SortType.ASC;
import static com.opus.opus.modules.contest.domain.SortType.CUSTOM;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_TEAM_ID_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_CONTEST_SORT_CUSTOM_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.VOTE_END_PRECEDE_VOTE_START;
import static com.opus.opus.modules.team.exception.TeamExceptionType.INVALID_ITEM_ORDER;
import static com.opus.opus.team.TeamFixture.createTeamWithContestIdAndItemOrder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSortRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestCommandService contestCommandService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestSortRepository contestSortRepository;
    @Autowired
    private TeamRepository teamRepository;

    private Contest contest;
    private static final Integer MAX_VOTES_LIMIT = 5;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(createContest());
        contestSortRepository.save(createContestSort(contest));
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

        assertThatThrownBy(() -> {
            contestCommandService.updateVotePeriod(contest.getId(), request);
        })
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

    @Test
    @DisplayName("[성공] 대회 정렬 설정 변경을 하면 설정이 변경된다.")
    void 대회_정렬_설정_변경을_하면_설정이_변경된다() {
        final ContestSort beforeContestSort = contestSortRepository.findByContestId(contest.getId()).orElseThrow();
        final ContestSortRequest request = new ContestSortRequest(ASC);

        contestCommandService.updateContestSort(contest.getId(), request);

        final ContestSort afterContestSort = contestSortRepository.findByContestId(contest.getId()).orElseThrow();
        assertThat(afterContestSort.getMode()).isEqualTo(ASC);
        assertThat(afterContestSort.getMode()).isNotEqualTo(beforeContestSort);
    }

    @Test
    @DisplayName("[성공] 팀 수동 정렬을 하면 정렬 순서가 바뀐다.")
    void 팀_수동_정렬을_하면_정렬_순서가_바뀐다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        contestCommandService.updateContestSortCustom(contest.getId(), requests);

        assertThat(teamOne.getItemOrder()).isEqualTo(2);
        assertThat(teamTwo.getItemOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("[실패] CUSTOM모드가 아니라면 수동 정렬은 실패한다.")
    void CUSTOM모드가_아니라면_수동_정렬은_실패한다() {
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(ONLY_CUSTOM_MODE_CAN_CHANGE.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 teamId가 있다면 수동 정렬은 실패한다.")
    void 중복_teamId가_있다면_수동_정렬은_실패한다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamOne.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(DUPLICATE_TEAM_ID_IN_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 중복 itemOrder가 있다면 수동 정렬은 실패한다.")
    void 중복_itemOrder가_있다면_수동_정렬은_실패한다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 1),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 list size가 팀 개수와 다르면 수동 정렬은 실패한다.")
    void 요청받은_list_size가_팀_개수와_다르면_수동_정렬은_실패한다() {
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(),3));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 2),
                new ContestSortCustomRequest(teamTwo.getId(), 1));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(ContestException.class).hasMessage(INVALID_CONTEST_SORT_CUSTOM_REQUEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 요청받은 itemOrder가 팀 개수를 넘어가면 수동 정렬은 실패한다.")
    void 요청받은_itemOrder가_팀_개수를_넘어가면_수동_정렬은_실패한다() {
        final int invalidItemOrder = 99;
        contestCommandService.updateContestSort(contest.getId(), new ContestSortRequest(CUSTOM));
        final Team teamOne = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 1));
        final Team teamTwo = teamRepository.save(createTeamWithContestIdAndItemOrder(contest.getId(), 2));
        final List<ContestSortCustomRequest> requests = List.of(new ContestSortCustomRequest(teamOne.getId(), 1),
                new ContestSortCustomRequest(teamTwo.getId(), invalidItemOrder));

        assertThatThrownBy(() -> {
            contestCommandService.updateContestSortCustom(contest.getId(), requests);
        }).isInstanceOf(TeamException.class).hasMessage(INVALID_ITEM_ORDER.errorMessage());
    }
}
