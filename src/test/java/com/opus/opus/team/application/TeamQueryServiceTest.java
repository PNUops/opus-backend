package com.opus.opus.team.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
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
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
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
    @DisplayName("[성공] 대회의 팀별 프로젝트 등록 현황을 조회할 수 있다.")
    void 대회의_팀별_프로젝트_등록_현황을_조회할_수_있다() {
        final Team submittedTeam = teamRepository.save(TeamFixture.createSubmittedTeamWithContestId(contest.getId()));

        final List<ContestSubmissionResponse> responseList = teamQueryService.getTeamSubmissions(contest.getId());
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

        final List<ContestSubmissionResponse> responseList = teamQueryService.getTeamSubmissions(emptyContest.getId());

        assertThat(responseList).isEmpty();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 등록 현황 조회 시 예외가 발생한다.")
    void 존재하지_않는_대회의_등록_현황_조회_시_예외가_발생한다() {
        final long invalidContestId = 999L;

        assertThatThrownBy(() -> teamQueryService.getTeamSubmissions(invalidContestId))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }
}
