package com.opus.opus.member.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.member.application.StatisticsQueryService;
import com.opus.opus.modules.member.application.dto.response.StatisticsSummaryResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamLikeFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StatisticsQueryServiceTest extends IntegrationTest {

    @Autowired
    private StatisticsQueryService statisticsQueryService;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamLikeRepository teamLikeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("[성공] 통계 요약을 정상적으로 조회할 수 있다.")
    void 통계_요약을_정상적으로_조회할_수_있다() {
        final Contest contest1 = contestRepository.save(ContestFixture.createContest());
        final Contest contest2 = contestRepository.save(ContestFixture.createContest());
        final Team team1 = teamRepository.save(TeamFixture.createSubmittedTeamWithContestId(contest1.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createSubmittedTeamWithContestId(contest2.getId()));
        final Member member = memberRepository.save(MemberFixture.createMember());
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team1, member.getId(), true));
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team2, member.getId(), true));

        final StatisticsSummaryResponse response = statisticsQueryService.getStatisticsSummary();

        assertThat(response.totalContests()).isEqualTo(2);
        assertThat(response.totalProjects()).isEqualTo(2);
        assertThat(response.totalLikes()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 데이터가 없으면 모두 0을 반환한다.")
    void 데이터가_없으면_모두_0을_반환한다() {
        final StatisticsSummaryResponse response = statisticsQueryService.getStatisticsSummary();

        assertThat(response.totalContests()).isZero();
        assertThat(response.totalProjects()).isZero();
        assertThat(response.totalLikes()).isZero();
    }

    @Test
    @DisplayName("[성공] 미제출 팀은 프로젝트 수에 포함되지 않는다.")
    void 미제출_팀은_프로젝트_수에_포함되지_않는다() {
        final Contest contest = contestRepository.save(ContestFixture.createContest());
        teamRepository.save(TeamFixture.createSubmittedTeamWithContestId(contest.getId()));
        teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));

        final StatisticsSummaryResponse response = statisticsQueryService.getStatisticsSummary();

        assertThat(response.totalProjects()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 취소된 좋아요는 좋아요 수에 포함되지 않는다.")
    void 취소된_좋아요는_좋아요_수에_포함되지_않는다() {
        final Contest contest = contestRepository.save(ContestFixture.createContest());
        final Team team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Member member1 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        final Member member2 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(2));
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member1.getId(), true));
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member2.getId(), false));

        final StatisticsSummaryResponse response = statisticsQueryService.getStatisticsSummary();

        assertThat(response.totalLikes()).isEqualTo(1);
    }
}
