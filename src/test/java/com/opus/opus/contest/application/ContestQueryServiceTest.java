package com.opus.opus.contest.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.team.TeamFixture;
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
    private ContestCategoryRepository contestCategoryRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ContestCategory category;
    private Contest contest;
    private Member member;

    @BeforeEach
    void setUp() {
        category = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contest = contestRepository.save(ContestFixture.createContest(category.getId()));
        member = memberRepository.save(MemberFixture.createMember());
    }

    @Test
    @DisplayName("[성공] 대회의 팀 목록을 조회할 수 있다.")
    void 대회의_팀_목록을_조회할_수_있다() {
        final Team team1 = teamRepository.save(TeamFixture.createTeam(contest.getId()));
        final Team team2 = teamRepository.save(TeamFixture.createTeam(contest.getId()));

        final List<TeamSummaryResponse> responses = contestQueryService.getContestTeamSummaries(contest.getId(),
                member);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(TeamSummaryResponse::teamId)
                .containsExactlyInAnyOrder(team1.getId(), team2.getId());
    }
}
