package com.opus.opus.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamVoteFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.antlr.v4.runtime.misc.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

public class TeamQueryServiceTest extends IntegrationTest {

    @Autowired
    private TeamQueryService teamQueryService;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

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
    @DisplayName("[성공] 팀 포스터 이미지를 조회한다.")
    void 팀_포스터_이미지를_조회한다() {
        // given
        final File file = File.builder()
                .referenceId(team.getId())
                .referenceType(TEAM)
                .imageType(POSTER)
                .filePath("path/to/poster.webp")
                .name("poster.jpg")
                .build();
        final File savedFile = fileRepository.save(file);
        savedFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(savedFile);

        Resource resource = new ByteArrayResource("content".getBytes());
        given(fileStorageUtil.findFileAndType(savedFile.getId()))
                .willReturn(new Pair<>(resource, "image/webp"));

        // when
        ImageResponse response = teamQueryService.getPosterImage(team.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.contentType()).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("[실패] 팀이 존재하지 않으면 포스터 이미지를 조회할 수 없다.")
    void 팀이_존재하지_않으면_포스터_이미지를_조회할_수_없다() {
        // given
        long notExistTeamId = 999L;

        // when & then
        assertThatThrownBy(() -> teamQueryService.getPosterImage(notExistTeamId))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 팀 포스터 이미지가 존재하지 않으면 조회할 수 없다.")
    void 팀_포스터_이미지가_존재하지_않으면_조회할_수_없다() {
        // when & then
        assertThatThrownBy(() -> teamQueryService.getPosterImage(team.getId()))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_EXISTS_MATCHING_IMAGE_ID.errorMessage());
    }

    @Test
    @DisplayName("[성공] 사용자의 남은 투표 개수를 조회할 수 있다.")
    void 사용자의_남은_투표_개수를_조회할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        MemberVoteCountResponse response = teamQueryService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 투표하지 않은 사용자는 최대 투표 수만큼 남은 투표 개수가 있다.")
    void 투표하지_않은_사용자는_최대_투표_수만큼_남은_투표_개수가_있다() {
        MemberVoteCountResponse response = teamQueryService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(2L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 취소한 투표는 카운트에서 제외된다.")
    void 취소한_투표는_카운트에서_제외된다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        Team secondTeam = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(secondTeam, member.getId(), false));

        MemberVoteCountResponse response = teamQueryService.getMemberVoteCount(member.getId(), contest.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[성공] Dense Ranking 방식으로 대회 내 팀들의 순위를 조회할 수 있다.")
    void dense_ranking_방식으로_팀들의_순위를_조회할_수_있다() {
        Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        Team team3 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 101L, true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 102L, true)); // team1: 2표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, 103L, true)); // team2: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team3, 104L, true)); // team3: 1표

        List<ContestRankingResponse> responses = teamQueryService.getTeamRanking(contest.getId());

        assertThat(responses).hasSize(4); // team1, team2, team3, setUp에서 만든 team
        assertThat(responses.get(0).rank()).isEqualTo(1);
        assertThat(responses.get(0).voteCount()).isEqualTo(2);
        assertThat(responses.get(1).rank()).isEqualTo(2);
        assertThat(responses.get(1).voteCount()).isEqualTo(1);
        assertThat(responses.get(2).rank()).isEqualTo(2);
        assertThat(responses.get(2).voteCount()).isEqualTo(1);
        assertThat(responses.get(3).rank()).isEqualTo(3);
        assertThat(responses.get(3).voteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("[성공] 투표가 없는 팀도 랭킹에 포함된다.")
    void 투표가_없는_팀도_랭킹에_포함된다() {
        List<ContestRankingResponse> responses = teamQueryService.getTeamRanking(contest.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).rank()).isEqualTo(1);
        assertThat(responses.get(0).voteCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("[성공] 대회의 투표 집계를 조회할 수 있다.")
    void 대회의_투표_집계를_조회할_수_있다() {
        Team team1 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        Team team2 = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, member.getId(), true)); // team1: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, member.getId(), true)); // team2: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 999L, true)); // team1: 1표

        ContestVoteStatisticsResponse response = teamQueryService.getVoteStatistics(contest.getId());

        assertThat(response.totalVotes()).isEqualTo(3L);
        assertThat(response.totalVoters()).isEqualTo(2L);
        assertThat(response.averageVotesPerVoter()).isEqualTo(1.5);
    }

    @Test
    @DisplayName("[성공] 투표가 없는 경우 집계 수치는 0으로 반환된다.")
    void 투표가_없는_경우_집계는_0이다() {
        ContestVoteStatisticsResponse response = teamQueryService.getVoteStatistics(contest.getId());

        assertThat(response.totalVotes()).isEqualTo(0L);
        assertThat(response.totalVoters()).isEqualTo(0L);
        assertThat(response.averageVotesPerVoter()).isEqualTo(0.0);
    }
}
