package com.opus.opus.team.application;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.modules.file.application.FileQueryService;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.TeamDetailResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.file.FileFixture;
import com.opus.opus.team.TeamLikeFixture;
import com.opus.opus.team.TeamVoteFixture;
import java.time.LocalDateTime;
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
    private TeamQueryService teamQueryService;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;
    @Autowired
    private TeamLikeRepository teamLikeRepository;

    @Autowired
    private FileQueryService fileQueryService;

    private Team team;
    private Contest contest;
    private ContestTrack track;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        team = teamRepository.save(Team.builder()
                .contestId(contest.getId())
                .trackId(track.getId())
                .teamName("팀명")
                .projectName("프로젝트명")
                .itemOrder(1)
                .teamAwards(new java.util.ArrayList<>())
                .teamMembers(new java.util.ArrayList<>())
                .build());
    }

    @Test
    @DisplayName("[성공] 비회원이 팀 상세 정보를 조회하면 isVoted와 isLiked는 항상 false를 반환한다.")
    void 비회원_팀_상세_정보_조회() {
        // given
        contest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        contestRepository.saveAndFlush(contest);

        // when
        final TeamDetailResponse response = teamQueryService.getTeamDetailPublic(team.getId());

        // then
        assertThat(response.isVoted()).isFalse();
        assertThat(response.isLiked()).isFalse();
    }

    @Test
    @DisplayName("[성공] 투표 기간일 때 팀 상세 정보를 조회하면 isVoted는 실제 투표 여부, isLiked는 false를 반환한다.")
    void 투표_기간일_때_팀_상세_정보_조회() {
        // given
        final Member member = memberRepository.save(com.opus.opus.member.MemberFixture.createMember());
        contest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        contestRepository.saveAndFlush(contest);

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), true));

        // when
        final TeamDetailResponse response = teamQueryService.getTeamDetail(team.getId(), member);

        // then
        assertThat(response.isVoted()).isTrue();
        assertThat(response.isLiked()).isFalse();
    }

    @Test
    @DisplayName("[성공] 투표 기간이 아닐 때 팀 상세 정보를 조회하면 isVoted는 false, isLiked는 실제 좋아요 여부를 반환한다.")
    void 투표_기간이_아닐_때_팀_상세_정보_조회() {
        // given
        final Member member = memberRepository.save(com.opus.opus.member.MemberFixture.createMember());
        contest.updateVotePeriod(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        contestRepository.saveAndFlush(contest);

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), true));

        // when
        final TeamDetailResponse response = teamQueryService.getTeamDetail(team.getId(), member);

        // then
        assertThat(response.isVoted()).isFalse();
        assertThat(response.isLiked()).isTrue();
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
        given(fileQueryService.findFileAndType(savedFile.getId()))
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
    @DisplayName("[성공] 팀 썸네일 이미지가 있으면 팀 썸네일을 반환한다.")
    void 팀_썸네일_조회_성공() {
        // given
        final File teamFile = fileRepository.save(FileFixture.createTeamThumbnailFile(team.getId()));
        teamFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(teamFile);

        given(fileQueryService.findFileAndType(teamFile.getId()))
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

        given(fileQueryService.findFileAndType(trackFile.getId()))
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
        given(fileQueryService.findDefaultThumbnail())
                .willReturn(new Pair<>(new ByteArrayResource("default".getBytes()), "image/jpeg"));

        // when
        ImageResponse response = teamQueryService.getThumbnailImage(team.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(new String(((ByteArrayResource) response.resource()).getByteArray())).isEqualTo("default");
    }
}
