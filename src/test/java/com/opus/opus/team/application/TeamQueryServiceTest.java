package com.opus.opus.team.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
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
import com.opus.opus.team.FileFixture;
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
import org.springframework.test.util.ReflectionTestUtils;

public class TeamQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestQueryService contestQueryService;
    @Autowired
    private  TeamQueryService teamQueryService;

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
    private ContestTrackRepository contestTrackRepository;

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
        final File file = FileFixture.createTeamPosterFile();
        ReflectionTestUtils.setField(file, "referenceId", team.getId());
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
    @DisplayName("[성공] 팀 썸네일 이미지가 있으면 팀 썸네일을 반환한다.")
    void 팀_썸네일_조회_성공() {
        // given
        final File teamFile = fileRepository.save(FileFixture.createTeamThumbnailFile(team.getId()));
        teamFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(teamFile);

        given(fileStorageUtil.findFileAndType(teamFile.getId()))
                .willReturn(new Pair<>(new ByteArrayResource("team".getBytes()), "image/webp"));

        // when
        ImageResponse response = teamQueryService.getThumbnailImage(team.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(new String(((ByteArrayResource) response.resource()).getByteArray())).isEqualTo("team");
    }

    @Test
    @DisplayName("[성공] 팀 썸네일이 없고 분과 썸네일이 있으면 분과 썸네일을 반환한다.")
    void 분과_썸네일_조회_성공() {
        // given
        Contest contest = contestRepository.save(ContestFixture.createContest());
        ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));

        ReflectionTestUtils.setField(team, "trackId", track.getId());
        teamRepository.saveAndFlush(team);

        final File trackFile = fileRepository.save(FileFixture.createTrackThumbnailFile(track.getId()));
        trackFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(trackFile);

        given(fileStorageUtil.findFileAndType(trackFile.getId()))
                .willReturn(new Pair<>(new ByteArrayResource("track".getBytes()), "image/webp"));

        // when
        ImageResponse response = teamQueryService.getThumbnailImage(team.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(new String(((ByteArrayResource) response.resource()).getByteArray())).isEqualTo("track");
    }

    @Test
    @DisplayName("[성공] 팀/분과 썸네일이 모두 없으면 시스템 기본 썸네일을 반환한다.")
    void 시스템_기본_썸네일_조회_성공() {
        // given
        given(fileStorageUtil.findDefaultThumbnail())
                .willReturn(new Pair<>(new ByteArrayResource("default".getBytes()), "image/jpeg"));

        // when
        ImageResponse response = teamQueryService.getThumbnailImage(team.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(new String(((ByteArrayResource) response.resource()).getByteArray())).isEqualTo("default");
    }

    @Test
    @DisplayName("[성공] Dense Ranking 방식으로 대회 내 팀들의 순위를 조회할 수 있다.")
    void dense_ranking_방식으로_팀들의_순위를_조회할_수_있다() {
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
        assertThat(responses.get(3).rank()).isEqualTo(3);
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

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, member.getId(), true)); // team1: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, member.getId(), true)); // team2: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 999L, true)); // team1: 1표

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
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team1, 101L, true)); // team1: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team2, 102L, true)); // team2: 1표
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team3, 103L, true)); // team3: 1표

        // 투표 수가 모두 1표로 동일 → team ID 오름차순 정렬 확인
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
        assertThat(firstTeam.teamId()).isEqualTo(team.getId()); // setUp에서 생성한 팀
        assertThat(firstTeam.isSubmitted()).isFalse();
        assertThat(secondTeam.teamId()).isEqualTo(submittedTeam.getId());
        assertThat(secondTeam.isSubmitted()).isTrue();
    }

    @Test
    @DisplayName("[성공] 팀이 없는 대회는 빈 리스트를 반환한다.")
    void 팀이_없는_대회는_빈_리스트를_반환한다() {
        final Contest emptyContest = contestRepository.save(ContestFixture.createContest());

        final List<ContestSubmissionResponse> responseList = contestQueryService.getTeamSubmissions(emptyContest.getId());

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
}
