package com.opus.opus.contest.application;

import static com.opus.opus.member.MemberFixture.createMemberWithUniqueNum;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_학생;
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
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestVoteQueryService;
import com.opus.opus.modules.contest.application.dto.response.AdmissionYearResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteReportResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.DailyVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.HourlyVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.TrackRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.TrackVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.VoteUsageResponse;
import com.opus.opus.modules.contest.application.dto.response.VoterRoleResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamVoteFixture;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

public class ContestVoteQueryServiceTest extends IntegrationTest {

    // 이미 마감된 투표 기간 (10일 전 0시 ~ 다음 날 13시, 13시 정각 마감이라 시간대는 12시까지 37개)
    private static final LocalDateTime ENDED_VOTE_START_AT = now().minusDays(10).truncatedTo(DAYS);
    private static final LocalDateTime ENDED_VOTE_END_AT = ENDED_VOTE_START_AT.plusDays(1).plusHours(13);

    @Autowired
    private ContestVoteQueryService contestVoteQueryService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private EntityManager entityManager;

    private Contest contest;
    private Team team;
    private Member member;

    @BeforeEach
    void setUp() {
        final Contest newContest = ContestFixture.createContest();
        newContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        newContest.updateMaxVotesLimit(2);

        contest = contestRepository.save(newContest);
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
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

        final Page<ContestVoteLogResponse> page = contestVoteQueryService.getContestVoteLog(contest.getId(), 0, 20);

        final List<ContestVoteLogResponse> contestVoteLogResponses = page.getContent();

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(contestVoteLogResponses.size()).isEqualTo(3);
        assertThat(contestVoteLogResponses.get(0).votedAt()).isAfterOrEqualTo(contestVoteLogResponses.get(1).votedAt());
    }

    @Test
    @DisplayName("[성공] 탈퇴한 회원의 표는 투표 로그에서 이름과 이메일 없이 조회된다.")
    void 탈퇴한_회원의_표는_투표_로그에서_이름과_이메일_없이_조회된다() {
        final Member withdrawnMember = memberRepository.save(createMemberWithUniqueNum(1));
        teamVoteRepository.save(createTeamVote(team, withdrawnMember.getId()));
        memberRepository.delete(withdrawnMember);
        entityManager.flush();
        entityManager.clear();

        final Page<ContestVoteLogResponse> page = contestVoteQueryService.getContestVoteLog(contest.getId(), 0, 20);

        assertThat(page.getContent())
                .extracting(ContestVoteLogResponse::voterName, ContestVoteLogResponse::voterEmail,
                        ContestVoteLogResponse::teamName)
                .containsExactly(tuple(null, null, team.getTeamName()));
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

        final List<ContestRankingResponse> responses = contestVoteQueryService.getTeamRanking(contest.getId());

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
        final List<ContestRankingResponse> responses = contestVoteQueryService.getTeamRanking(contest.getId());

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

        final ContestVoteStatisticsResponse response = contestVoteQueryService.getVoteStatistics(contest.getId());

        assertThat(response.totalVotes()).isEqualTo(3L);
        assertThat(response.totalVoters()).isEqualTo(2L);
        assertThat(response.averageVotesPerVoter()).isEqualTo(1.5);
    }

    @Test
    @DisplayName("[성공] 투표가 없는 경우 집계 수치는 0으로 반환된다.")
    void 투표가_없는_경우_집계는_0이다() {
        final ContestVoteStatisticsResponse response = contestVoteQueryService.getVoteStatistics(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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
        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());
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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());
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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());
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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

        assertThat(response.trackVotes())
                .extracting(TrackVoteResponse::trackName, TrackVoteResponse::teamCount,
                        TrackVoteResponse::voteCount, TrackVoteResponse::averageVotesPerTeam)
                .containsExactly(
                        tuple(track.getTrackName(), 2, 3L, 1.5),
                        tuple(null, 1, 0L, 0.0)
                );
    }

    @Test
    @DisplayName("[성공] 투표 리포트에서 입학연도별 투표자 구성을 조회하고 학생이 아닌 투표자는 학번이 숫자여도 따로 집계된다.")
    void 투표_리포트에서_입학연도별_투표자_구성을_조회하고_학생이_아닌_투표자는_학번이_숫자여도_따로_집계된다() {
        final Member student2021 = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202112345@pusan.ac.kr", ROLE_학생));
        final Member otherStudent2021 = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202154321@pusan.ac.kr", ROLE_학생));
        final Member student2023 = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("202312345@pusan.ac.kr", ROLE_학생));
        final Member professor = memberRepository.save(
                MemberFixture.createMemberWithEmailAndRole("201012345@pusan.ac.kr", ROLE_교수));
        teamVoteRepository.save(createTeamVote(team, student2021.getId()));
        teamVoteRepository.save(createTeamVote(team, otherStudent2021.getId()));
        teamVoteRepository.save(createTeamVote(team, student2023.getId()));
        teamVoteRepository.save(createTeamVote(team, professor.getId()));

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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

        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());
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
        final ContestVoteReportResponse response = contestVoteQueryService.getVoteReport(contest.getId());

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
            contestVoteQueryService.getVoteReport(invalidContestId);
        }).isInstanceOf(ContestException.class).hasMessage(NOT_FOUND_CONTEST.errorMessage());
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

        final List<ContestRankingResponse> responses = contestVoteQueryService.getTeamRanking(contest.getId());
        List<ContestRankingResponse> sameVoteResponses = responses.stream()
                .filter(r -> r.voteCount() == 1L)
                .toList();

        for (int i = 0; i < sameVoteResponses.size() - 1; i++) {
            assertThat(sameVoteResponses.get(i).teamId())
                    .isLessThan(sameVoteResponses.get(i + 1).teamId());
        }
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
}
