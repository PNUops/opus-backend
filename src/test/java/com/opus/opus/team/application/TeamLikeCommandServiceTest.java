package com.opus.opus.team.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_LIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_UNLIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.NOT_LIKED_YET;
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
import com.opus.opus.modules.team.application.TeamLikeCommandService;
import com.opus.opus.modules.team.application.dto.response.TeamLikeToggleResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamLikeException;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamLikeFixture;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TeamLikeCommandServiceTest extends IntegrationTest {

    @Autowired
    private TeamLikeCommandService teamLikeCommandService;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamLikeRepository teamLikeRepository;
    @Autowired
    private ContestRepository contestRepository;

    private Contest contest;
    private Team team;
    private Member member;

    @BeforeEach
    void setUp() {
        final Contest newContest = ContestFixture.createContest();
        newContest.updateVotePeriod(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        contest = contestRepository.save(newContest);

        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
    }

    @Test
    @DisplayName("[성공] 처음 좋아요하면 TeamLike가 생성되고 좋아요가 등록된다.")
    void 처음_좋아요하면_TeamLike가_생성되고_좋아요가_등록된다() {
        final TeamLikeToggleResponse response = teamLikeCommandService.toggleLike(member.getId(), team.getId(), true);

        assertThat(response.teamId()).isEqualTo(team.getId());
        assertThat(response.isLiked()).isTrue();
        assertThat(response.message()).isEqualTo("좋아요가 등록되었습니다.");

        final TeamLike savedLike = teamLikeRepository.findByMemberIdAndTeam(member.getId(), team).orElseThrow();
        assertThat(savedLike.getIsLiked()).isTrue();
    }

    @Test
    @DisplayName("[성공] 기존 좋아요를 취소할 수 있다.")
    void 기존_좋아요를_취소할_수_있다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), true));

        final TeamLikeToggleResponse response = teamLikeCommandService.toggleLike(member.getId(), team.getId(), false);

        assertThat(response.isLiked()).isFalse();
        assertThat(response.message()).isEqualTo("좋아요가 취소되었습니다.");
    }

    @Test
    @DisplayName("[성공] 취소한 좋아요를 다시 등록할 수 있다.")
    void 취소한_좋아요를_다시_등록할_수_있다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), false));

        final TeamLikeToggleResponse response = teamLikeCommandService.toggleLike(member.getId(), team.getId(), true);

        assertThat(response.isLiked()).isTrue();
        assertThat(response.message()).isEqualTo("좋아요가 등록되었습니다.");
    }

    @Test
    @DisplayName("[실패] 좋아요한 적 없는 팀에 취소 요청하면 예외가 발생한다.")
    void 좋아요한_적_없는_팀에_취소_요청하면_예외가_발생한다() {
        assertThatThrownBy(() -> teamLikeCommandService.toggleLike(member.getId(), team.getId(), false))
                .isInstanceOf(TeamLikeException.class)
                .hasMessage(NOT_LIKED_YET.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이미 좋아요한 팀에 다시 좋아요하면 예외가 발생한다.")
    void 이미_좋아요한_팀에_다시_좋아요하면_예외가_발생한다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), true));

        assertThatThrownBy(() -> teamLikeCommandService.toggleLike(member.getId(), team.getId(), true))
                .isInstanceOf(TeamLikeException.class)
                .hasMessage(ALREADY_LIKED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이미 좋아요 취소한 팀에 다시 취소하면 예외가 발생한다.")
    void 이미_좋아요_취소한_팀에_다시_취소하면_예외가_발생한다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), false));

        assertThatThrownBy(() -> teamLikeCommandService.toggleLike(member.getId(), team.getId(), false))
                .isInstanceOf(TeamLikeException.class)
                .hasMessage(ALREADY_UNLIKED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에는 좋아요할 수 없다.")
    void 존재하지_않는_팀에는_좋아요할_수_없다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> teamLikeCommandService.toggleLike(member.getId(), invalidTeamId, true))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 기간에는 좋아요할 수 없다.")
    void 투표_기간에는_좋아요할_수_없다() {
        final Contest votingContest = ContestFixture.createContest();
        votingContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        final Contest savedVotingContest = contestRepository.save(votingContest);

        final Team votingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(savedVotingContest.getId()));

        assertThatThrownBy(() -> teamLikeCommandService.toggleLike(member.getId(), votingTeam.getId(), true))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_DURING_VOTING_PERIOD.errorMessage());
    }
}
