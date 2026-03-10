package com.opus.opus.contest.application;

import static com.opus.opus.member.MemberFixture.createMemberWithUniqueNum;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestTemplateExceptionType.NOT_FOUND_TEMPLATE;
import static com.opus.opus.team.TeamFixture.createTeamWithContestId;
import static com.opus.opus.team.TeamVoteFixture.createTeamVote;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTemplateFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestTemplateResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTemplateRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestTemplateException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamVoteFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

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
    @Autowired
    private ContestTemplateRepository contestTemplateRepository;
    @Autowired
    private ContestCategoryRepository contestCategoryRepository;

    private Contest contest;
    private Team team;
    private Member member;

    @BeforeEach
    void setUp() {
        Contest newContest = ContestFixture.createContest();
        newContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        newContest.updateMaxVotesLimit(2);

        contest = contestRepository.save(newContest);
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
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
        final Contest newContest = contestRepository.save(ContestFixture.createContest());
        final ContestVotesLimitResponse response = contestQueryService.getMaxVotesLimit(newContest.getId());

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

        final Page<ContestVoteLogResponse> page = contestQueryService.getContestVoteLog(contest.getId(), 0, 20);

        final List<ContestVoteLogResponse> contestVoteLogResponses = page.getContent();

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(contestVoteLogResponses.size()).isEqualTo(3);
        assertThat(contestVoteLogResponses.get(0).votedAt()).isAfterOrEqualTo(contestVoteLogResponses.get(1).votedAt());
    }

    @Test
    @DisplayName("[성공] 사용자의 남은 투표 개수를 조회할 수 있다.")
    void 사용자의_남은_투표_개수를_조회할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        MemberVoteCountResponse response = contestQueryService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 투표하지 않은 사용자는 최대 투표 수만큼 남은 투표 개수가 있다.")
    void 투표하지_않은_사용자는_최대_투표_수만큼_남은_투표_개수가_있다() {
        MemberVoteCountResponse response = contestQueryService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(2L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 취소한 투표는 카운트에서 제외된다.")
    void 취소한_투표는_카운트에서_제외된다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        Team secondTeam = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(secondTeam, member.getId(), false));

        MemberVoteCountResponse response = contestQueryService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[성공] Standard Ranking 방식으로 대회 내 팀들의 순위를 조회할 수 있다.")
    void standard_ranking_방식으로_팀들의_순위를_조회할_수_있다() {
        final Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team3 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 101L, true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 102L, true)); // team1: 2표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, 103L, true)); // team2: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team3, 104L, true)); // team3: 1표

        final List<ContestRankingResponse> responses = contestQueryService.getTeamRanking(contest.getId());

        assertThat(responses).hasSize(4); // team1, team2, team3, setUp에서 만든 team
        assertThat(responses.get(0).rank()).isEqualTo(1);
        assertThat(responses.get(0).voteCount()).isEqualTo(2);
        assertThat(responses.get(1).rank()).isEqualTo(2);
        assertThat(responses.get(1).voteCount()).isEqualTo(1);
        assertThat(responses.get(2).rank()).isEqualTo(2);
        assertThat(responses.get(2).voteCount()).isEqualTo(1);
        assertThat(responses.get(3).rank()).isEqualTo(4);
        assertThat(responses.get(3).voteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("[성공] 투표가 없는 팀도 랭킹에 포함된다.")
    void 투표가_없는_팀도_랭킹에_포함된다() {
        final List<ContestRankingResponse> responses = contestQueryService.getTeamRanking(contest.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).rank()).isEqualTo(1);
        assertThat(responses.get(0).voteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("[성공] 대회의 투표 집계를 조회할 수 있다.")
    void 대회의_투표_집계를_조회할_수_있다() {
        final Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, member.getId(), true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, member.getId(), true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 999L, true));

        final ContestVoteStatisticsResponse response = contestQueryService.getVoteStatistics(contest.getId());

        assertThat(response.totalVotes()).isEqualTo(3L);
        assertThat(response.totalVoters()).isEqualTo(2L);
        assertThat(response.averageVotesPerVoter()).isEqualTo(1.5);
    }

    @Test
    @DisplayName("[성공] 투표가 없는 경우 집계 수치는 0으로 반환된다.")
    void 투표가_없는_경우_집계는_0이다() {
        final ContestVoteStatisticsResponse response = contestQueryService.getVoteStatistics(contest.getId());

        assertThat(response.totalVotes()).isEqualTo(0L);
        assertThat(response.totalVoters()).isEqualTo(0L);
        assertThat(response.averageVotesPerVoter()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("[성공] 투표 수가 같은 팀은 팀 ID 오름차순으로 정렬된다.")
    void 투표_수가_같은_팀은_팀_ID_오름차순으로_정렬된다() {
        final Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team3 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 101L, true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, 102L, true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team3, 103L, true));

        final List<ContestRankingResponse> responses = contestQueryService.getTeamRanking(contest.getId());
        List<ContestRankingResponse> sameVoteResponses = responses.stream()
                .filter(r -> r.voteCount() == 1L)
                .toList();

        for (int i = 0; i < sameVoteResponses.size() - 1; i++) {
            assertThat(sameVoteResponses.get(i).teamId())
                    .isLessThan(sameVoteResponses.get(i + 1).teamId());
        }
    }

    @Test
    @DisplayName("[성공] 대회의 팀별 프로젝트 등록 현황을 조회할 수 있다.")
    void 대회의_팀별_프로젝트_등록_현황을_조회할_수_있다() {
        final Team submittedTeam = teamRepository.save(TeamFixture.createSubmittedTeamWithContestId(contest.getId()));

        final List<ContestSubmissionResponse> responseList = contestQueryService.getTeamSubmissions(contest.getId());
        final ContestSubmissionResponse firstTeam = responseList.get(0);
        final ContestSubmissionResponse secondTeam = responseList.get(1);

        assertThat(responseList).hasSize(2);
        assertThat(firstTeam.teamId()).isEqualTo(team.getId());
        assertThat(firstTeam.isSubmitted()).isFalse();
        assertThat(secondTeam.teamId()).isEqualTo(submittedTeam.getId());
        assertThat(secondTeam.isSubmitted()).isTrue();
    }

    @Test
    @DisplayName("[성공] 팀이 없는 대회는 빈 리스트를 반환한다.")
    void 팀이_없는_대회는_빈_리스트를_반환한다() {
        final Contest emptyContest = contestRepository.save(ContestFixture.createContest());

        final List<ContestSubmissionResponse> responseList = contestQueryService.getTeamSubmissions(
                emptyContest.getId());

        assertThat(responseList).isEmpty();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 등록 현황 조회 시 예외가 발생한다.")
    void 존재하지_않는_대회의_등록_현황_조회_시_예외가_발생한다() {
        final long invalidContestId = 999L;

        assertThatThrownBy(() -> contestQueryService.getTeamSubmissions(invalidContestId))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 대회 템플릿을 조회한다.")
    void 대회_템플릿을_조회한다() {
        contestTemplateRepository.save(ContestTemplateFixture.createContestTemplate(contest));

        final ContestTemplateResponse response = contestQueryService.getContestTemplate(contest.getId());

        assertThat(response.trackRequired()).isTrue();
        assertThat(response.projectNameRequired()).isTrue();
        assertThat(response.teamNameRequired()).isTrue();
        assertThat(response.leaderRequired()).isTrue();
        assertThat(response.teamMembersRequired()).isTrue();
        assertThat(response.professorRequired()).isTrue();
        assertThat(response.githubPathRequired()).isTrue();
        assertThat(response.youTubePathRequired()).isTrue();
        assertThat(response.productionPathRequired()).isTrue();
        assertThat(response.overviewRequired()).isTrue();
        assertThat(response.posterRequired()).isTrue();
        assertThat(response.imagesRequired()).isTrue();
    }

    @Test
    @DisplayName("[실패] 대회 템플릿이 존재하지 않으면 예외가 발생한다.")
    void 대회_템플릿이_존재하지_않으면_예외가_발생한다() {
        assertThatThrownBy(() -> {
            contestQueryService.getContestTemplate(contest.getId());
        }).isInstanceOf(ContestTemplateException.class).hasMessage(NOT_FOUND_TEMPLATE.errorMessage());
    }

    @Test
    @DisplayName("[성공] 대회의 팀 목록을 조회할 수 있다.")
    void 대회의_팀_목록을_조회할_수_있다() {
        final Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));

        final List<TeamSummaryResponse> responses = contestQueryService.getContestTeamSummaries(contest.getId(),
                member);

        assertThat(responses).hasSize(3); // setup에서 저장된 team 포함
        assertThat(responses)
                .extracting(TeamSummaryResponse::teamId)
                .containsExactlyInAnyOrder(team.getId(), team1.getId(), team2.getId());
    }
}
