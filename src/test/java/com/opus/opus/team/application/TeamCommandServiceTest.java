package com.opus.opus.team.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_VOTE_PERIOD_NOW;
import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_UNVOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_VOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.NOT_VOTED_YET;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamVoteException;
import com.opus.opus.team.FileFixture;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamVoteFixture;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

public class TeamCommandServiceTest extends IntegrationTest {

    @Autowired
    private TeamCommandService teamCommandService;

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
    @DisplayName("[성공] 팀 포스터 이미지를 저장한다.")
    void 팀_포스터_이미지를_저장한다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "poster.jpg", "image/jpeg",
                "content".getBytes());

        // when
        teamCommandService.savePosterImage(team.getId(), image);

        // then
        verify(fileStorageUtil, times(1)).storeFile(any(), eq(team.getId()), eq(TEAM), eq(POSTER));
    }

    @Test
    @DisplayName("[실패] 팀이 존재하지 않으면 포스터 이미지를 저장할 수 없다.")
    void 팀이_존재하지_않으면_포스터_이미지를_저장할_수_없다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "poster.jpg", "image/jpeg",
                "content".getBytes());
        final long notExistTeamId = 999L;

        // when & then
        assertThatThrownBy(() -> teamCommandService.savePosterImage(notExistTeamId, image))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지를 삭제한다.")
    void 팀_포스터_이미지를_삭제한다() {
        // given
        final File file = FileFixture.createTeamPosterFile();
        setField(file, "referenceId", team.getId());
        final File savedFile = fileRepository.save(file);
        savedFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(savedFile);

        // when
        teamCommandService.deletePosterImage(team.getId());

        // then
        verify(fileStorageUtil, times(1)).deleteFile(savedFile.getId());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지가 없어도 삭제 요청 시 예외가 발생하지 않는다.")
    void 팀_포스터_이미지가_없어도_삭제_요청_시_예외가_발생하지_않는다() {
        // when
        teamCommandService.deletePosterImage(team.getId());

        // then
        verify(fileStorageUtil, never()).deleteFile(any());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지가 이미 존재하면 기존 이미지를 삭제하고 새로 저장한다.")
    void 팀_포스터_이미지가_이미_존재하면_기존_이미지를_삭제하고_새로_저장한다() {
        // given
        final File existingFile = FileFixture.createTeamPosterFile();
        setField(existingFile, "referenceId", team.getId());
        final File savedFile = fileRepository.save(existingFile);

        final MockMultipartFile newImage = new MockMultipartFile("image", "new_poster.jpg", "image/jpeg",
                "new_content".getBytes());

        // when
        teamCommandService.savePosterImage(team.getId(), newImage);

        // then
        verify(fileStorageUtil, times(1)).deleteFile(savedFile.getId());
        verify(fileStorageUtil, times(1)).storeFile(any(), eq(team.getId()), eq(TEAM), eq(POSTER));
    }

    @Test
    @DisplayName("[성공] 처음 투표하면 TeamVote가 생성되고 투표가 등록된다.")
    void 처음_투표하면_TeamVote가_생성되고_투표가_등록된다() {
        TeamVoteToggleResponse response = teamCommandService.toggleVote(member.getId(), team.getId(), true);

        assertThat(response.teamId()).isEqualTo(team.getId());
        assertThat(response.isVoted()).isTrue();
        assertThat(response.message()).isEqualTo("투표가 등록되었습니다.");
        assertThat(response.remainingVotesCount()).isEqualTo(1L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);

        TeamVote savedVote = teamVoteRepository.findByMemberIdAndTeam(member.getId(), team).orElseThrow();
        assertThat(savedVote.getIsVoted()).isTrue();
    }

    @Test
    @DisplayName("[성공] 기존 투표를 취소할 수 있다.")
    void 기존_투표를_취소할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        TeamVoteToggleResponse response = teamCommandService.toggleVote(member.getId(), team.getId(), false);

        assertThat(response.isVoted()).isFalse();
        assertThat(response.message()).isEqualTo("투표가 취소되었습니다.");
        assertThat(response.remainingVotesCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 취소한 투표를 다시 등록할 수 있다.")
    void 취소한_투표를_다시_등록할_수_있다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), false));

        TeamVoteToggleResponse response = teamCommandService.toggleVote(member.getId(), team.getId(), true);

        assertThat(response.isVoted()).isTrue();
        assertThat(response.message()).isEqualTo("투표가 등록되었습니다.");
        assertThat(response.remainingVotesCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[실패] 투표한 적 없는 팀에 취소 요청하면 예외가 발생한다.")
    void 투표한_적_없는_팀에_취소_요청하면_예외가_발생한다() {
        assertThatThrownBy(() -> teamCommandService.toggleVote(member.getId(), team.getId(), false))
                .isInstanceOf(TeamVoteException.class)
                .hasMessage(NOT_VOTED_YET.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이미 투표한 팀에 다시 투표하면 예외가 발생한다.")
    void 이미_투표한_팀에_다시_투표하면_예외가_발생한다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        assertThatThrownBy(() -> teamCommandService.toggleVote(member.getId(), team.getId(), true))
                .isInstanceOf(TeamVoteException.class)
                .hasMessage(ALREADY_VOTED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 이미 투표 취소한 팀에 다시 취소하면 예외가 발생한다.")
    void 이미_투표_취소한_팀에_다시_취소하면_예외가_발생한다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), false));

        assertThatThrownBy(() -> teamCommandService.toggleVote(member.getId(), team.getId(), false))
                .isInstanceOf(TeamVoteException.class)
                .hasMessage(ALREADY_UNVOTED.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에는 투표할 수 없다.")
    void 존재하지_않는_팀에는_투표할_수_없다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> teamCommandService.toggleVote(member.getId(), invalidTeamId, true))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 기간이 아니면 투표할 수 없다.")
    void 투표_기간이_아니면_투표할_수_없다() {
        Contest notVotingContest = ContestFixture.createContest();
        notVotingContest.updateVotePeriod(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        notVotingContest.updateMaxVotesLimit(2);
        notVotingContest = contestRepository.save(notVotingContest);

        Team notVotingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(notVotingContest.getId()));

        assertThatThrownBy(() -> teamCommandService.toggleVote(member.getId(), notVotingTeam.getId(), true))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_VOTE_PERIOD_NOW.errorMessage());
    }

    @Test
    @DisplayName("[실패] 최대 투표 수를 초과하면 예외가 발생한다.")
    void 최대_투표_수를_초과하면_예외가_발생한다() {
        Team secondTeam = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        Team thirdTeam = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(secondTeam, member.getId(), true));

        assertThatThrownBy(() -> teamCommandService.toggleVote(member.getId(), thirdTeam.getId(), true))
                .isInstanceOf(TeamVoteException.class)
                .hasMessageContaining("최대 2개 팀만 투표할 수 있습니다.");
    }
}
