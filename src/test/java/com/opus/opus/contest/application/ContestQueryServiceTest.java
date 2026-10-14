package com.opus.opus.contest.application;

import static com.opus.opus.member.MemberFixture.createMemberWithUniqueNum;
import static com.opus.opus.modules.contest.domain.SortType.CUSTOM;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestTemplateExceptionType.NOT_FOUND_TEMPLATE;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_학생;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static com.opus.opus.team.TeamFixture.createTeamWithContestId;
import static com.opus.opus.team.TeamFixture.createTeamWithContestIdAndTrackId;
import static com.opus.opus.team.TeamVoteFixture.createTeamVote;
import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSortFixture;
import com.opus.opus.contest.ContestSubmissionFeedbackFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestTemplateFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.response.AdmissionYearResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestTemplateResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteReportResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.DailyVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.HourlyVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamDashboardSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.TrackRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.TrackVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.application.dto.response.VoteUsageResponse;
import com.opus.opus.modules.contest.application.dto.response.VoterRoleResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSortRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTemplateRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestTemplateException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamVoteFixture;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

public class ContestQueryServiceTest extends IntegrationTest {

    // 이미 마감된 투표 기간 (10일 전 0시 ~ 다음 날 13시, 13시 정각 마감이라 시간대는 12시까지 37개)
    private static final LocalDateTime ENDED_VOTE_START_AT = now().minusDays(10).truncatedTo(DAYS);
    private static final LocalDateTime ENDED_VOTE_END_AT = ENDED_VOTE_START_AT.plusDays(1).plusHours(13);

    @Autowired
    private ContestQueryService contestQueryService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;
    @Autowired
    private ContestTemplateRepository contestTemplateRepository;
    @Autowired
    private ContestCategoryRepository contestCategoryRepository;
    @Autowired
    private ContestSortRepository contestSortRepository;
    @Autowired
    private ContestSubmissionItemRepository submissionItemRepository;
    @Autowired
    private ContestSubmissionRepository submissionRepository;
    @Autowired
    private ContestSubmissionFeedbackRepository feedbackRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private EntityManager entityManager;

    private Contest contest;
    private Team team;
    private Member member;
    private ContestSort contestSort;

    @BeforeEach
    void setUp() {
        Contest newContest = ContestFixture.createContest();
        newContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        newContest.updateMaxVotesLimit(2);

        contest = contestRepository.save(newContest);
        contestSort = contestSortRepository.save(ContestSortFixture.createContestSort(contest));
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(ROLE_팀원))
                .build());
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

        teamVoteRepository.save(createTeamVote(team, member.getId()));
        teamVoteRepository.save(createTeamVote(otherTeam, member.getId()));
        teamVoteRepository.save(createTeamVote(team, otherMember.getId()));

        final Page<ContestVoteLogResponse> page = contestQueryService.getContestVoteLog(contest.getId(), 0, 20);

        final List<ContestVoteLogResponse> contestVoteLogResponses = page.getContent();

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(contestVoteLogResponses.size()).isEqualTo(3);
        assertThat(contestVoteLogResponses.get(0).votedAt()).isAfterOrEqualTo(contestVoteLogResponses.get(1).votedAt());
    }

    @Test
    @DisplayName("[성공] 사용자의 남은 투표 개수를 조회할 수 있다.")
    void 사용자의_남은_투표_개수를_조회할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId()));

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
    @DisplayName("[성공] 한 표만 사용한 사용자는 한 표가 남는다.")
    void 한_표만_사용한_사용자는_한_표가_남는다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId()));

        MemberVoteCountResponse response = contestQueryService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[성공] Standard Ranking 방식으로 대회 내 팀들의 순위를 조회할 수 있다.")
    void standard_ranking_방식으로_팀들의_순위를_조회할_수_있다() {
        final Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team3 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 101L));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 102L)); // team1: 2표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, 103L)); // team2: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team3, 104L)); // team3: 1표

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

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, member.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, member.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 999L));

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
    @DisplayName("[성공] 투표 리포트에서 요약 수치를 조회할 수 있다.")
    void 투표_리포트에서_요약_수치를_조회할_수_있다() {
        final Team team2 = teamRepository.save(createTeamWithContestId(contest.getId()));
        final Team team3 = teamRepository.save(createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(createTeamVote(team, 101L));
        teamVoteRepository.save(createTeamVote(team2, 101L));
        teamVoteRepository.save(createTeamVote(team, 102L));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.contestName()).isEqualTo(contest.getContestName());
        assertThat(response.isVoteEnded()).isFalse();
        assertThat(response.maxVotesLimit()).isEqualTo(2);
        assertThat(response.totalVoters()).isEqualTo(2L);
        assertThat(response.totalVotes()).isEqualTo(3L);
        assertThat(response.averageVotesPerVoter()).isEqualTo(1.5);
        assertThat(response.teamCount()).isEqualTo(3);
        assertThat(response.averageVotesPerTeam()).isEqualTo(1.0);
        assertThat(response.zeroVoteTeamCount()).isEqualTo(1);
        assertThat(response.rankings()).extracting(ContestRankingResponse::teamId)
                .containsExactly(team.getId(), team2.getId(), team3.getId());
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 일자별 투표자 수와 투표 수를 조회할 수 있다.")
    void 투표_리포트에서_일자별_투표자_수와_투표_수를_조회할_수_있다() {
        final Team team2 = teamRepository.save(createTeamWithContestId(contest.getId()));
        updateVotePeriodToEnded();
        saveVoteAt(team, 101L, ENDED_VOTE_START_AT.plusHours(10).plusMinutes(15));
        saveVoteAt(team2, 101L, ENDED_VOTE_START_AT.plusDays(1).plusHours(9).plusMinutes(30));
        saveVoteAt(team, 102L, ENDED_VOTE_START_AT.plusDays(1).plusHours(9).plusMinutes(45));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.dailyVotes())
                .extracting(DailyVoteResponse::date, DailyVoteResponse::voterCount, DailyVoteResponse::voteCount)
                .containsExactly(
                        tuple(ENDED_VOTE_START_AT.toLocalDate(), 1L, 1L),
                        tuple(ENDED_VOTE_START_AT.toLocalDate().plusDays(1), 2L, 2L)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 표가 없는 날도 0으로 채워 일자별 투표를 조회한다.")
    void 투표_리포트에서_표가_없는_날도_0으로_채워_일자별_투표를_조회한다() {
        contest.updateVotePeriod(ENDED_VOTE_START_AT, ENDED_VOTE_END_AT.plusDays(1));
        saveVoteAt(team, 101L, ENDED_VOTE_START_AT.plusHours(10));
        saveVoteAt(team, 102L, ENDED_VOTE_START_AT.plusDays(2).plusHours(9));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.dailyVotes())
                .extracting(DailyVoteResponse::date, DailyVoteResponse::voterCount, DailyVoteResponse::voteCount)
                .containsExactly(
                        tuple(ENDED_VOTE_START_AT.toLocalDate(), 1L, 1L),
                        tuple(ENDED_VOTE_START_AT.toLocalDate().plusDays(1), 0L, 0L),
                        tuple(ENDED_VOTE_START_AT.toLocalDate().plusDays(2), 1L, 1L)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 사용한 투표 수별 투표자 분포를 조회할 수 있다.")
    void 투표_리포트에서_사용한_투표_수별_투표자_분포를_조회할_수_있다() {
        final Team team2 = teamRepository.save(createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(createTeamVote(team, 101L));
        teamVoteRepository.save(createTeamVote(team2, 101L));
        teamVoteRepository.save(createTeamVote(team, 102L));
        teamVoteRepository.save(createTeamVote(team, 103L));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.voteUsages())
                .extracting(VoteUsageResponse::usedVotes, VoteUsageResponse::voterCount, VoteUsageResponse::ratio)
                .containsExactly(
                        tuple(1L, 2L, 66.7),
                        tuple(2L, 1L, 33.3)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 최대 투표 수보다 많이 투표한 투표자가 있으면 그 투표 수까지 분포를 조회한다.")
    void 투표_리포트에서_최대_투표_수보다_많이_투표한_투표자가_있으면_그_투표_수까지_분포를_조회한다() {
        final Team team2 = teamRepository.save(createTeamWithContestId(contest.getId()));
        final Team team3 = teamRepository.save(createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(createTeamVote(team, 101L));
        teamVoteRepository.save(createTeamVote(team2, 101L));
        teamVoteRepository.save(createTeamVote(team3, 101L));
        teamVoteRepository.save(createTeamVote(team, 102L));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.voteUsages())
                .extracting(VoteUsageResponse::usedVotes, VoteUsageResponse::voterCount, VoteUsageResponse::ratio)
                .containsExactly(
                        tuple(1L, 1L, 50.0),
                        tuple(2L, 0L, 0.0),
                        tuple(3L, 1L, 50.0)
                );
    }

    @Test
    @DisplayName("[성공] 투표 마감 전에는 현재 시각까지 중간 집계한 투표 리포트를 조회한다.")
    void 투표_마감_전에는_현재_시각까지_중간_집계한_투표_리포트를_조회한다() {
        teamVoteRepository.save(createTeamVote(team, 101L));

        final LocalDateTime requestedAt = now();
        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());
        final List<HourlyVoteResponse> hourlyVotes = response.hourlyVotes();

        assertThat(response.isVoteEnded()).isFalse();
        assertThat(response.aggregatedAt()).isBetween(requestedAt, now());
        assertThat(hourlyVotes.get(0).hour()).isEqualTo(contest.getVoteStartAt().truncatedTo(HOURS));
        assertThat(hourlyVotes.get(hourlyVotes.size() - 1))
                .extracting(HourlyVoteResponse::hour, HourlyVoteResponse::cumulativeVoterCount)
                .containsExactly(response.aggregatedAt().truncatedTo(HOURS), 1L);
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 투표 기간 밖에 남은 표가 있으면 그 시각까지 시간대별 집계 범위를 넓힌다.")
    void 투표_리포트에서_투표_기간_밖에_남은_표가_있으면_그_시각까지_시간대별_집계_범위를_넓힌다() {
        updateVotePeriodToEnded();
        saveVoteAt(team, 101L, ENDED_VOTE_START_AT.minusHours(2).plusMinutes(30));
        saveVoteAt(team, 102L, ENDED_VOTE_END_AT.plusHours(3).plusMinutes(10));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());
        final List<HourlyVoteResponse> hourlyVotes = response.hourlyVotes();

        assertThat(hourlyVotes).hasSize(43);
        assertThat(List.of(hourlyVotes.get(0), hourlyVotes.get(hourlyVotes.size() - 1)))
                .extracting(HourlyVoteResponse::hour, HourlyVoteResponse::voteCount,
                        HourlyVoteResponse::cumulativeVoterCount)
                .containsExactly(
                        tuple(ENDED_VOTE_START_AT.minusHours(2), 1L, 1L),
                        tuple(ENDED_VOTE_END_AT.plusHours(3), 1L, 2L)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 투표 기간 동안 시간대별 투표 수와 누적 투표자 수를 조회할 수 있다.")
    void 투표_리포트에서_투표_기간_동안_시간대별_투표_수와_누적_투표자_수를_조회할_수_있다() {
        final Team team2 = teamRepository.save(createTeamWithContestId(contest.getId()));
        updateVotePeriodToEnded();
        saveVoteAt(team, 101L, ENDED_VOTE_START_AT.plusHours(10).plusMinutes(15));
        saveVoteAt(team2, 101L, ENDED_VOTE_START_AT.plusDays(1).plusHours(9).plusMinutes(30));
        saveVoteAt(team, 102L, ENDED_VOTE_START_AT.plusDays(1).plusHours(9).plusMinutes(45));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());
        final List<HourlyVoteResponse> hourlyVotes = response.hourlyVotes();

        assertThat(response.isVoteEnded()).isTrue();
        assertThat(response.aggregatedAt()).isEqualTo(ENDED_VOTE_END_AT);
        assertThat(hourlyVotes).hasSize(37);
        assertThat(hourlyVotes.get(0).hour()).isEqualTo(ENDED_VOTE_START_AT);
        assertThat(hourlyVotes.get(hourlyVotes.size() - 1).hour()).isEqualTo(ENDED_VOTE_END_AT.minusHours(1));
        assertThat(hourlyVotes)
                .filteredOn(hourlyVote -> hourlyVote.voteCount() > 0)
                .extracting(HourlyVoteResponse::hour, HourlyVoteResponse::voteCount,
                        HourlyVoteResponse::cumulativeVoterCount)
                .containsExactly(
                        tuple(ENDED_VOTE_START_AT.plusHours(10), 1L, 1L),
                        tuple(ENDED_VOTE_START_AT.plusDays(1).plusHours(9), 2L, 2L)
                );
        assertThat(hourlyVotes.get(hourlyVotes.size() - 1).cumulativeVoterCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 대표 역할별 투표자 구성을 조회하고 탈퇴한 투표자는 역할 없이 집계된다.")
    void 투표_리포트에서_대표_역할별_투표자_구성을_조회하고_탈퇴한_투표자는_역할_없이_집계된다() {
        final Member student = memberRepository.save(MemberFixture.createMemberWithRole("학생", 1, ROLE_학생));
        final Member withdrawnStudent = memberRepository.save(MemberFixture.createMemberWithRole("탈퇴", 2, ROLE_학생));
        final Member professor = memberRepository.save(MemberFixture.createMemberWithRole("교수", 3, ROLE_교수));
        teamVoteRepository.save(createTeamVote(team, student.getId()));
        teamVoteRepository.save(createTeamVote(team, withdrawnStudent.getId()));
        teamVoteRepository.save(createTeamVote(team, professor.getId()));
        memberRepository.delete(withdrawnStudent);
        entityManager.flush();
        entityManager.clear();

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.voterRoles())
                .extracting(VoterRoleResponse::role, VoterRoleResponse::voterCount, VoterRoleResponse::ratio)
                .containsExactly(
                        tuple("ROLE_학생", 1L, 33.3),
                        tuple("ROLE_교수", 1L, 33.3),
                        tuple(null, 1L, 33.3)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 분과별 팀 수와 득표 수를 조회할 수 있다.")
    void 투표_리포트에서_분과별_팀_수와_득표_수를_조회할_수_있다() {
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final Team trackTeam1 = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        final Team trackTeam2 = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        ReflectionTestUtils.setField(team, "trackId", null);
        teamVoteRepository.save(createTeamVote(trackTeam1, 101L));
        teamVoteRepository.save(createTeamVote(trackTeam2, 101L));
        teamVoteRepository.save(createTeamVote(trackTeam1, 102L));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.trackVotes())
                .extracting(TrackVoteResponse::trackName, TrackVoteResponse::teamCount,
                        TrackVoteResponse::voteCount, TrackVoteResponse::averageVotesPerTeam)
                .containsExactly(
                        tuple(track.getTrackName(), 2, 3L, 1.5),
                        tuple(null, 1, 0L, 0.0)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 입학연도별 투표자 구성을 조회하고 학번이 없는 투표자는 따로 집계된다.")
    void 투표_리포트에서_입학연도별_투표자_구성을_조회하고_학번이_없는_투표자는_따로_집계된다() {
        final Member student2021 = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202112345@pusan.ac.kr", ROLE_학생));
        final Member otherStudent2021 = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202154321@pusan.ac.kr", ROLE_학생));
        final Member student2023 = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202312345@pusan.ac.kr", ROLE_학생));
        final Member professor = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("professor@pusan.ac.kr", ROLE_교수));
        teamVoteRepository.save(createTeamVote(team, student2021.getId()));
        teamVoteRepository.save(createTeamVote(team, otherStudent2021.getId()));
        teamVoteRepository.save(createTeamVote(team, student2023.getId()));
        teamVoteRepository.save(createTeamVote(team, professor.getId()));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.admissionYears())
                .extracting(AdmissionYearResponse::admissionYear, AdmissionYearResponse::voterCount,
                        AdmissionYearResponse::ratio)
                .containsExactly(
                        tuple(2021, 2L, 50.0),
                        tuple(2023, 1L, 25.0),
                        tuple(null, 1L, 25.0)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 학번이 없거나 4자리 미만이거나 탈퇴한 투표자는 입학연도 없이 집계된다.")
    void 투표_리포트에서_학번이_없거나_4자리_미만이거나_탈퇴한_투표자는_입학연도_없이_집계된다() {
        final Member student2022 = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202212345@pusan.ac.kr", ROLE_학생));
        final Member shortStudentIdMember = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("123@pusan.ac.kr", ROLE_학생));
        final Member socialMember = memberRepository.save(
                MemberFixture.createSocialMember("social@gmail.com", "google-social-id"));
        final Member withdrawnStudent = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202112345@pusan.ac.kr", ROLE_학생));
        teamVoteRepository.save(createTeamVote(team, student2022.getId()));
        teamVoteRepository.save(createTeamVote(team, shortStudentIdMember.getId()));
        teamVoteRepository.save(createTeamVote(team, socialMember.getId()));
        teamVoteRepository.save(createTeamVote(team, withdrawnStudent.getId()));
        memberRepository.delete(withdrawnStudent);
        entityManager.flush();
        entityManager.clear();

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.admissionYears())
                .extracting(AdmissionYearResponse::admissionYear, AdmissionYearResponse::voterCount,
                        AdmissionYearResponse::ratio)
                .containsExactly(
                        tuple(2022, 1L, 25.0),
                        tuple(null, 3L, 75.0)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 분과 안 순위로 득표한 분과별 1~3위를 동점 팀까지 조회할 수 있다.")
    void 투표_리포트에서_분과_안_순위로_득표한_분과별_1_3위를_동점_팀까지_조회할_수_있다() {
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final Team first = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        final Team second = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        final Team tiedThird = teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        final Team otherTiedThird = teamRepository.save(
                createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        teamRepository.save(createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        ReflectionTestUtils.setField(team, "trackId", null);
        teamVoteRepository.save(createTeamVote(first, 101L));
        teamVoteRepository.save(createTeamVote(first, 102L));
        teamVoteRepository.save(createTeamVote(first, 103L));
        teamVoteRepository.save(createTeamVote(second, 101L));
        teamVoteRepository.save(createTeamVote(second, 102L));
        teamVoteRepository.save(createTeamVote(tiedThird, 103L));
        teamVoteRepository.save(createTeamVote(otherTiedThird, 104L));
        teamVoteRepository.save(createTeamVote(team, 105L));

        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());
        final List<TrackRankingResponse> trackTopRankings = response.trackTopRankings();

        assertThat(trackTopRankings).extracting(TrackRankingResponse::trackName)
                .containsExactly(track.getTrackName(), null);
        assertThat(trackTopRankings.get(0).rankings())
                .extracting(ContestRankingResponse::rank, ContestRankingResponse::teamId)
                .containsExactly(
                        tuple(1, first.getId()),
                        tuple(2, second.getId()),
                        tuple(3, tiedThird.getId()),
                        tuple(3, otherTiedThird.getId())
                );
        assertThat(trackTopRankings.get(1).rankings())
                .extracting(ContestRankingResponse::rank, ContestRankingResponse::teamId)
                .containsExactly(tuple(1, team.getId()));
    }

    @Test
    @DisplayName("[성공] 투표가 없는 경우 투표 리포트의 집계 수치는 0으로 반환된다.")
    void 투표가_없는_경우_투표_리포트의_집계_수치는_0으로_반환된다() {
        final ContestVoteReportResponse response = contestQueryService.getVoteReport(contest.getId());

        assertThat(response.totalVoters()).isEqualTo(0L);
        assertThat(response.totalVotes()).isEqualTo(0L);
        assertThat(response.averageVotesPerVoter()).isEqualTo(0.0);
        assertThat(response.dailyVotes())
                .extracting(DailyVoteResponse::voteCount)
                .containsOnly(0L);
        assertThat(response.voterRoles()).isEmpty();
        assertThat(response.trackTopRankings())
                .flatExtracting(TrackRankingResponse::rankings)
                .isEmpty();
        assertThat(response.admissionYears()).isEmpty();
        assertThat(response.voteUsages())
                .extracting(VoteUsageResponse::voterCount)
                .containsOnly(0L);
        assertThat(response.hourlyVotes())
                .extracting(HourlyVoteResponse::voteCount)
                .containsOnly(0L);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 투표 리포트는 조회할 수 없다.")
    void 존재하지_않는_대회의_투표_리포트는_조회할_수_없다() {
        final Long invalidContestId = 999L;

        assertThatThrownBy(() -> {
            contestQueryService.getVoteReport(invalidContestId);
        }).isInstanceOf(ContestException.class).hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    private void updateVotePeriodToEnded() {
        contest.updateVotePeriod(ENDED_VOTE_START_AT, ENDED_VOTE_END_AT);
    }

    private void saveVoteAt(final Team votedTeam, final Long memberId, final LocalDateTime votedAt) {
        final TeamVote teamVote = teamVoteRepository.save(createTeamVote(votedTeam, memberId));
        entityManager.flush();

        entityManager.createNativeQuery("UPDATE team_vote SET created_at = :votedAt WHERE id = :id")
                .setParameter("votedAt", votedAt)
                .setParameter("id", teamVote.getId())
                .executeUpdate();
        entityManager.clear();
    }

    @Test
    @DisplayName("[성공] 투표 수가 같은 팀은 팀 ID 오름차순으로 정렬된다.")
    void 투표_수가_같은_팀은_팀_ID_오름차순으로_정렬된다() {
        final Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Team team3 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 101L));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, 102L));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team3, 103L));

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

    @Test
    @DisplayName("[성공] CUSTOM 모드이면 itemOrder 오름차순으로 정렬된다.")
    void CUSTOM_모드이면_itemOrder_오름차순으로_정렬된다() {
        final Contest localContest = contestRepository.save(ContestFixture.createContest());
        final ContestSort localContestSort = contestSortRepository.save(
                ContestSortFixture.createContestSort(localContest));
        localContestSort.updateMode(CUSTOM);

        final Team third = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndItemOrder(localContest.getId(), 3));
        final Team first = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndItemOrder(localContest.getId(), 1));
        final Team second = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndItemOrder(localContest.getId(), 2));

        final List<TeamSummaryResponse> responses = contestQueryService.getContestTeamSummaries(localContest.getId(),
                member);

        assertThat(responses)
                .extracting(TeamSummaryResponse::teamId)
                .containsExactly(first.getId(), second.getId(), third.getId());
    }

    @Test
    @DisplayName("[성공] 미제출 항목이 있으면 pendingSubmissionCount에 반영된다.")
    void 미제출_항목이_있으면_pendingSubmissionCount에_반영된다() {
        submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));

        final TeamDashboardSummaryResponse response =
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), member);

        assertThat(response.pendingSubmissionCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[성공] 이미 제출한 항목은 pendingSubmissionCount에 포함되지 않는다.")
    void 이미_제출한_항목은_pendingSubmissionCount에_포함되지_않는다() {
        final ContestSubmissionItem item = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item));

        final TeamDashboardSummaryResponse response =
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), member);

        assertThat(response.pendingSubmissionCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("[성공] 마감이 지난 항목은 pendingSubmissionCount에 포함되지 않는다.")
    void 마감이_지난_항목은_pendingSubmissionCount에_포함되지_않는다() {
        submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItemWithPastDeadline(contest));

        final TeamDashboardSummaryResponse response =
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), member);

        assertThat(response.pendingSubmissionCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("[성공] nearestDeadline은 미제출 항목 중 가장 가까운 endAt이다.")
    void nearestDeadline은_미제출_항목_중_가장_가까운_endAt이다() {
        final LocalDateTime nearDeadline = now().plusDays(3);
        final LocalDateTime farDeadline = now().plusDays(10);
        submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItemWithEndAt(contest, nearDeadline));
        submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItemWithEndAt(contest, farDeadline));

        final TeamDashboardSummaryResponse response =
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), member);

        assertThat(response.nearestDeadline()).isEqualToIgnoringNanos(nearDeadline);
    }

    @Test
    @DisplayName("[성공] 읽지 않은 피드백 수가 unreadFeedbackCount에 반영된다.")
    void 읽지_않은_피드백_수가_unreadFeedbackCount에_반영된다() {
        final ContestSubmissionItem item = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmission submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item));
        feedbackRepository.save(ContestSubmissionFeedbackFixture.createFeedback(submission, member.getId()));

        final TeamDashboardSummaryResponse response =
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), member);

        assertThat(response.unreadFeedbackCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[성공] 읽은 피드백은 unreadFeedbackCount에 포함되지 않는다.")
    void 읽은_피드백은_unreadFeedbackCount에_포함되지_않는다() {
        final ContestSubmissionItem item = submissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmission submission = submissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item));
        feedbackRepository.save(ContestSubmissionFeedbackFixture.createReadFeedback(submission, member.getId()));

        final TeamDashboardSummaryResponse response =
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), member);

        assertThat(response.unreadFeedbackCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("[성공] 피드백이 없으면 latestFeedbackPreview가 null이다.")
    void 피드백이_없으면_latestFeedbackPreview가_null이다() {
        final TeamDashboardSummaryResponse response =
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), member);

        assertThat(response.latestFeedbackPreview()).isNull();
    }

    @Test
    @DisplayName("[실패] 팀원이 아닌 사용자가 대시보드 통계를 조회하면 TEAM_MEMBER_NOT_FOUND_IN_TEAM 예외가 발생한다.")
    void 팀원이_아닌_사용자가_대시보드_통계를_조회하면_예외가_발생한다() {
        final Member outsider = memberRepository.save(MemberFixture.createMemberWithUniqueNum(9));

        assertThatThrownBy(() ->
                contestQueryService.getTeamDashboardSummary(contest.getId(), team.getId(), outsider))
                .isInstanceOf(TeamMemberException.class)
                .satisfies(e -> assertThat(((TeamMemberException) e).exceptionType())
                        .isEqualTo(TEAM_MEMBER_NOT_FOUND_IN_TEAM));
    }

}
