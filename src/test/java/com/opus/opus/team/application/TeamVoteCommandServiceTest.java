package com.opus.opus.team.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_VOTE_PERIOD_NOW;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_UNVOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_VOTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.TeamVoteCommandService;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamVoteException;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamVoteFixture;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TeamVoteCommandServiceTest extends IntegrationTest {

    @Autowired
    private TeamVoteCommandService teamVoteCommandService;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;
    @Autowired
    private ContestRepository contestRepository;

    private Contest contest;
    private Team team;
    private Member member;

    @BeforeEach
    void setUp() {
        Contest newContest = ContestFixture.createContest();
        newContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        newContest.updateMaxVotesLimit(2); // 최대 투표 수를 기본적으로 2로 설정
        contest = contestRepository.save(newContest);

        team = teamRepository.save(TeamFixture.createTeam(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
    }

    @Test
    @DisplayName("[성공] 처음 투표하면 TeamVote가 생성되고 투표가 등록된다.")
    void 처음_투표하면_TeamVote가_생성되고_투표가_등록된다() {
        TeamVoteToggleResponse response = teamVoteCommandService.toggleVote(member.getId(), team.getId(), true);

        assertThat(response.teamId()).isEqualTo(team.getId());
        assertThat(response.isVoted()).isTrue();
        assertThat(response.message()).isEqualTo("투표가 처음 등록되었습니다.");
        assertThat(response.remainingVotesCount()).isEqualTo(1L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);

        TeamVote savedVote = teamVoteRepository.findByMemberIdAndTeam(member.getId(), team).orElseThrow();
        assertThat(savedVote.getIsVoted()).isTrue();
    }

    @Test
    @DisplayName("[성공] 처음 요청이 isVoted=false이면 비활성화 상태로 초기화된다.(다만, 일반적으로는 사용하지 않는 플로우임)")
    void 처음_요청이_isVoted_false이면_비활성화_상태로_초기화된다() {
        TeamVoteToggleResponse response = teamVoteCommandService.toggleVote(member.getId(), team.getId(), false);

        assertThat(response.isVoted()).isFalse();
        assertThat(response.message()).isEqualTo("투표가 비활성화된 상태로 초기화되었습니다.");
        assertThat(response.remainingVotesCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 기존 투표를 취소할 수 있다.")
    void 기존_투표를_취소할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        MemberVoteCountResponse beforeResponse = teamVoteCommandService.getMemberVoteCount(member.getId(), contest.getId());
        assertThat(beforeResponse.remainingVotesCount()).isEqualTo(1L);

        TeamVoteToggleResponse afterResponse = teamVoteCommandService.toggleVote(member.getId(), team.getId(), false);

        assertThat(afterResponse.isVoted()).isFalse();
        assertThat(afterResponse.message()).isEqualTo("투표가 취소되었습니다.");
        assertThat(afterResponse.remainingVotesCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 취소한 투표를 다시 등록할 수 있다.")
    void 취소한_투표를_다시_등록할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), false));

        TeamVoteToggleResponse response = teamVoteCommandService.toggleVote(member.getId(), team.getId(), true);

        assertThat(response.isVoted()).isTrue();
        assertThat(response.message()).isEqualTo("투표가 등록되었습니다.");
        assertThat(response.remainingVotesCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[실패] 이미 투표한 팀에 다시 투표하면 예외가 발생한다.")
    void 이미_투표한_팀에_다시_투표하면_예외가_발생한다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        assertThatThrownBy(() -> teamVoteCommandService.toggleVote(member.getId(), team.getId(), true))
                .isInstanceOf(TeamVoteException.class)
                .hasMessage(ALREADY_VOTED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이미 투표 취소한 팀에 다시 취소하면 예외가 발생한다.")
    void 이미_투표_취소한_팀에_다시_취소하면_예외가_발생한다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), false));

        assertThatThrownBy(() -> teamVoteCommandService.toggleVote(member.getId(), team.getId(), false))
                .isInstanceOf(TeamVoteException.class)
                .hasMessage(ALREADY_UNVOTED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에는 투표할 수 없다.")
    void 존재하지_않는_팀에는_투표할_수_없다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> teamVoteCommandService.toggleVote(member.getId(), invalidTeamId, true))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 기간이 아니면 투표할 수 없다.")
    void 투표_기간이_아니면_투표할_수_없다() {
        Contest notVotingContest = ContestFixture.createContest();
        notVotingContest.updateVotePeriod(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        notVotingContest.updateMaxVotesLimit(2);
        notVotingContest = contestRepository.save(notVotingContest);

        Team notVotingTeam = teamRepository.save(TeamFixture.createTeam(notVotingContest.getId()));

        assertThatThrownBy(() -> teamVoteCommandService.toggleVote(member.getId(), notVotingTeam.getId(), true))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_VOTE_PERIOD_NOW.errorMessage());
    }

    @Test
    @DisplayName("[실패] 최대 투표 수를 초과하면 예외가 발생한다.")
    void 최대_투표_수를_초과하면_예외가_발생한다() {
        Team secondTeam = teamRepository.save(TeamFixture.createTeam(contest.getId()));
        Team thirdTeam = teamRepository.save(TeamFixture.createTeam(contest.getId()));

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(secondTeam, member.getId(), true));

        assertThatThrownBy(() -> teamVoteCommandService.toggleVote(member.getId(), thirdTeam.getId(), true))
                .isInstanceOf(TeamVoteException.class)
                .hasMessageContaining("최대 2개 팀만 투표할 수 있습니다.");
    }

    @Test
    @DisplayName("[성공] 사용자의 남은 투표 개수를 조회할 수 있다.")
    void 사용자의_남은_투표_개수를_조회할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        MemberVoteCountResponse response = teamVoteCommandService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 투표하지 않은 사용자는 최대 투표 수만큼 남은 투표 개수가 있다.")
    void 투표하지_않은_사용자는_최대_투표_수만큼_남은_투표_개수가_있다() {
        MemberVoteCountResponse response = teamVoteCommandService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(2L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 취소한 투표는 카운트에서 제외된다.")
    void 취소한_투표는_카운트에서_제외된다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        Team secondTeam = teamRepository.save(TeamFixture.createTeam(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(secondTeam, member.getId(), false));

        MemberVoteCountResponse response = teamVoteCommandService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
    }
}
