package com.opus.opus.team.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_VOTE_PERIOD_NOW;
import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTemplateFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTemplateRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestExceptionType;
import com.opus.opus.modules.contest.exception.ContestTrackException;
import com.opus.opus.modules.contest.exception.ContestTrackExceptionType;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.application.dto.request.TeamCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamUpdateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCreateResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamExceptionType;
import com.opus.opus.file.FileFixture;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.modules.team.exception.TeamMemberExceptionType;
import com.opus.opus.modules.team.exception.TeamVoteException;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamLikeFixture;
import com.opus.opus.team.TeamVoteFixture;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

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
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private TeamLikeRepository teamLikeRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private ContestTemplateRepository contestTemplateRepository;

    private Contest notVotingContest;
    private Contest votingContest;
    private Team notVotingTeam;
    private Team votingTeam;
    private Team generalTeam;
    private Member member;

    @BeforeEach
    void setUp() {
        generalTeam = teamRepository.save(TeamFixture.createTeam());
        member = memberRepository.save(MemberFixture.createMember());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(generalTeam)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀원))
                .build());

        final Contest newNotVotingContest = ContestFixture.createContest();
        newNotVotingContest.updateVotePeriod(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        notVotingContest = contestRepository.save(newNotVotingContest); // 투표 기간이 지난 대회 생성
        notVotingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(notVotingContest.getId()));

        final Contest newVotingContest = ContestFixture.createContest();
        newVotingContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(5));
        newVotingContest.updateMaxVotesLimit(2);
        votingContest = contestRepository.save(newVotingContest); // 투표 가능한 대회 생성
        votingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(votingContest.getId())); // 투표 가능한 팀 생성
    }

    @Test
    @DisplayName("[성공] 팀을 등록한다.")
    void 팀을_등록한다() {
        // given
        final TeamCreateRequest request = new TeamCreateRequest(
                votingContest.getId(), null, "프로젝트", "팀", "교수",
                "https://github.com/path", "https://youtube.com/path", "https://prod.path", "개요"
        );

        // when
        final TeamCreateResponse response = teamCommandService.createTeam(request);

        // then
        assertThat(response.teamId()).isNotNull();
        assertThat(teamRepository.findById(response.teamId())).isPresent();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회 ID로 팀을 등록하면 예외가 발생한다.")
    void 존재하지_않는_대회_ID로_팀을_등록하면_예외가_발생한다() {
        // given
        final Long invalidContestId = 999L;
        final TeamCreateRequest request = new TeamCreateRequest(
                invalidContestId, null, null, null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> teamCommandService.createTeam(request))
                .isInstanceOf(ContestException.class)
                .hasMessage(ContestExceptionType.NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 분과 ID로 팀을 등록하면 예외가 발생한다.")
    void 존재하지_않는_분과_ID로_팀을_등록하면_예외가_발생한다() {
        // given
        final Long invalidTrackId = 999L;
        final TeamCreateRequest request = new TeamCreateRequest(
                votingContest.getId(), invalidTrackId, null, null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> teamCommandService.createTeam(request))
                .isInstanceOf(ContestTrackException.class)
                .hasMessage(ContestTrackExceptionType.NOT_FOUND_TRACK.errorMessage());
    }

    @Test
    @DisplayName("[실패] 해당 대회에 속하지 않은 분과 ID로 팀을 등록하면 예외가 발생한다.")
    void 해당_대회에_속하지_않은_분과_ID로_팀을_등록하면_예외가_발생한다() {
        // given
        final Contest otherContest = contestRepository.save(ContestFixture.createContest());
        final ContestTrack otherTrack = contestTrackRepository.save(ContestTrackFixture.createTrack(otherContest));

        final TeamCreateRequest request = new TeamCreateRequest(
                votingContest.getId(), otherTrack.getId(), null, null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> teamCommandService.createTeam(request))
                .isInstanceOf(ContestTrackException.class)
                .hasMessage(ContestTrackExceptionType.INVALID_TRACK_FOR_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 팀을 삭제한다.")
    void 팀을_삭제한다() {
        // given
        final Long teamId = generalTeam.getId();

        // when
        teamCommandService.deleteTeam(teamId);

        // then
        assertThat(teamRepository.findById(teamId)).isEmpty();
    }

    @Test
    @DisplayName("[성공] 팀을 삭제하면 관련 이미지들도 모두 삭제된다.")
    void 팀을_삭제하면_관련_이미지들도_모두_삭제된다() {
        // given
        final Long teamId = generalTeam.getId();

        // 포스터, 썸네일, 프리뷰 파일 생성 및 저장
        final File poster = fileRepository.save(FileFixture.createTeamPosterFile(teamId));
        final File thumbnail = fileRepository.save(FileFixture.createTeamThumbnailFile(teamId));
        final File preview1 = fileRepository.save(FileFixture.createTeamPreviewFile(teamId));
        final File preview2 = fileRepository.save(FileFixture.createTeamPreviewFile(teamId));

        // when
        teamCommandService.deleteTeam(teamId);

        // then
        assertThat(teamRepository.findById(teamId)).isEmpty();

        // storage 삭제 검증
        verify(fileStorageUtil).deleteFile(poster.getId());
        verify(fileStorageUtil).deleteFile(thumbnail.getId());
        verify(fileStorageUtil).deleteFile(preview1.getId());
        verify(fileStorageUtil).deleteFile(preview2.getId());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지를 저장한다.")
    void 팀_포스터_이미지를_저장한다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "poster.jpg", "image/jpeg",
                "content".getBytes());

        // when
        teamCommandService.savePosterImage(generalTeam.getId(), image, member);

        // then
        verify(fileStorageUtil, times(1)).storeFile(any(), eq(generalTeam.getId()), eq(TEAM), eq(POSTER));
    }

    @Test
    @DisplayName("[실패] 팀이 존재하지 않으면 포스터 이미지를 저장할 수 없다.")
    void 팀이_존재하지_않으면_포스터_이미지를_저장할_수_없다() {
        // given
        final MockMultipartFile image = new MockMultipartFile("image", "poster.jpg", "image/jpeg",
                "content".getBytes());
        final long notExistTeamId = 999L;

        // when & then
        assertThatThrownBy(() -> teamCommandService.savePosterImage(notExistTeamId, image, member))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지를 삭제한다.")
    void 팀_포스터_이미지를_삭제한다() {
        // given
        final File file = FileFixture.createTeamPosterFile();
        setField(file, "referenceId", generalTeam.getId());
        final File savedFile = fileRepository.save(file);
        savedFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(savedFile);

        // when
        teamCommandService.deletePosterImage(generalTeam.getId(), member);

        // then
        verify(fileStorageUtil, times(1)).deleteFile(savedFile.getId());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지가 없어도 삭제 요청 시 예외가 발생하지 않는다.")
    void 팀_포스터_이미지가_없어도_삭제_요청_시_예외가_발생하지_않는다() {
        // when
        teamCommandService.deletePosterImage(generalTeam.getId(), member);

        // then
        verify(fileStorageUtil, never()).deleteFile(any());
    }

    @Test
    @DisplayName("[성공] 팀 포스터 이미지가 이미 존재하면 기존 이미지를 삭제하고 새로 저장한다.")
    void 팀_포스터_이미지가_이미_존재하면_기존_이미지를_삭제하고_새로_저장한다() {
        // given
        final File existingFile = FileFixture.createTeamPosterFile();
        setField(existingFile, "referenceId", generalTeam.getId());
        final File savedFile = fileRepository.save(existingFile);

        final MockMultipartFile newImage = new MockMultipartFile("image", "new_poster.jpg", "image/jpeg",
                "new_content".getBytes());

        // when
        teamCommandService.savePosterImage(generalTeam.getId(), newImage, member);

        // then
        verify(fileStorageUtil, times(1)).deleteFile(savedFile.getId());
        verify(fileStorageUtil, times(1)).storeFile(any(), eq(generalTeam.getId()), eq(TEAM), eq(POSTER));
    }

    @Test
    @DisplayName("[성공] 처음 투표하면 TeamVote가 생성되고 카운트가 반환된다.")
    void 처음_투표하면_TeamVote가_생성되고_카운트가_반환된다() {
        final TeamVoteResponse response = teamCommandService.addTeamVote(member.getId(), votingTeam.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
        assertThat(teamVoteRepository.existsByMemberIdAndTeam(member.getId(), votingTeam)).isTrue();
    }

    @Test
    @DisplayName("[성공] 이미 투표한 팀에 다시 투표하면 NO-OP이며 같은 카운트가 반환된다.")
    void 이미_투표한_팀에_다시_투표하면_NO_OP이다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(votingTeam, member.getId()));

        final TeamVoteResponse response = teamCommandService.addTeamVote(member.getId(), votingTeam.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
        assertThat(response.maxVotesLimit()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[성공] 기존 투표를 취소하면 TeamVote가 삭제된다.")
    void 기존_투표를_취소하면_TeamVote가_삭제된다() {
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(votingTeam, member.getId()));

        final TeamVoteResponse response = teamCommandService.removeTeamVote(member.getId(), votingTeam.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(2L);
        assertThat(teamVoteRepository.existsByMemberIdAndTeam(member.getId(), votingTeam)).isFalse();
    }

    @Test
    @DisplayName("[성공] 투표하지 않은 팀에 취소 요청하면 NO-OP이며 현재 카운트가 반환된다.")
    void 투표하지_않은_팀에_취소_요청하면_NO_OP이다() {
        final TeamVoteResponse response = teamCommandService.removeTeamVote(member.getId(), votingTeam.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(2L);
        assertThat(teamVoteRepository.existsByMemberIdAndTeam(member.getId(), votingTeam)).isFalse();
    }

    @Test
    @DisplayName("[성공] 투표 취소 후 다시 투표할 수 있다.")
    void 투표_취소_후_다시_투표할_수_있다() {
        teamCommandService.addTeamVote(member.getId(), votingTeam.getId());
        teamCommandService.removeTeamVote(member.getId(), votingTeam.getId());

        final TeamVoteResponse response = teamCommandService.addTeamVote(member.getId(), votingTeam.getId());

        assertThat(response.remainingVotesCount()).isEqualTo(1L);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에는 투표할 수 없다.")
    void 존재하지_않는_팀에는_투표할_수_없다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> teamCommandService.addTeamVote(member.getId(), invalidTeamId))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 기간이 아니면 투표를 등록할 수 없다.")
    void 투표_기간이_아니면_투표를_등록할_수_없다() {
        Contest notVotingContest = ContestFixture.createContest();
        notVotingContest.updateVotePeriod(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        notVotingContest.updateMaxVotesLimit(2);
        notVotingContest = contestRepository.save(notVotingContest);

        final Team notVotingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(notVotingContest.getId()));

        assertThatThrownBy(() -> teamCommandService.addTeamVote(member.getId(), notVotingTeam.getId()))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_VOTE_PERIOD_NOW.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 기간이 아니면 투표를 취소할 수 없다.")
    void 투표_기간이_아니면_투표를_취소할_수_없다() {
        Contest notVotingContest = ContestFixture.createContest();
        notVotingContest.updateVotePeriod(LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(5));
        notVotingContest.updateMaxVotesLimit(2);
        notVotingContest = contestRepository.save(notVotingContest);

        final Team notVotingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(notVotingContest.getId()));

        assertThatThrownBy(() -> teamCommandService.removeTeamVote(member.getId(), notVotingTeam.getId()))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_VOTE_PERIOD_NOW.errorMessage());
    }

    @Test
    @DisplayName("[실패] 최대 투표 수를 초과하면 예외가 발생한다.")
    void 최대_투표_수를_초과하면_예외가_발생한다() {
        final Team secondTeam = teamRepository.save(TeamFixture.createTeamWithContestId(votingContest.getId()));
        final Team thirdTeam = teamRepository.save(TeamFixture.createTeamWithContestId(votingContest.getId()));

        teamVoteRepository.save(TeamVoteFixture.createTeamVote(votingTeam, member.getId()));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(secondTeam, member.getId()));

        assertThatThrownBy(() -> teamCommandService.addTeamVote(member.getId(), thirdTeam.getId()))
                .isInstanceOf(TeamVoteException.class)
                .hasMessageContaining("최대 2개 팀만 투표할 수 있습니다.");
    }

    @Test
    @DisplayName("[성공] 처음 좋아요하면 TeamLike가 생성된다.")
    void 처음_좋아요하면_TeamLike가_생성된다() {
        teamCommandService.addTeamLike(member.getId(), notVotingTeam.getId());

        assertThat(teamLikeRepository.existsByMemberIdAndTeam(member.getId(), notVotingTeam)).isTrue();
    }

    @Test
    @DisplayName("[성공] 이미 좋아요한 팀에 다시 좋아요하면 NO-OP이다.")
    void 이미_좋아요한_팀에_다시_좋아요하면_NO_OP이다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(notVotingTeam, member.getId()));

        teamCommandService.addTeamLike(member.getId(), notVotingTeam.getId());

        assertThat(teamLikeRepository.existsByMemberIdAndTeam(member.getId(), notVotingTeam)).isTrue();
    }

    @Test
    @DisplayName("[성공] 기존 좋아요를 취소하면 TeamLike가 삭제된다.")
    void 기존_좋아요를_취소하면_TeamLike가_삭제된다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(notVotingTeam, member.getId()));

        teamCommandService.removeTeamLike(member.getId(), notVotingTeam.getId());

        assertThat(teamLikeRepository.existsByMemberIdAndTeam(member.getId(), notVotingTeam)).isFalse();
    }

    @Test
    @DisplayName("[성공] 좋아요하지 않은 팀에 취소 요청하면 NO-OP이다.")
    void 좋아요하지_않은_팀에_취소_요청하면_NO_OP이다() {
        teamCommandService.removeTeamLike(member.getId(), notVotingTeam.getId());

        assertThat(teamLikeRepository.existsByMemberIdAndTeam(member.getId(), notVotingTeam)).isFalse();
    }

    @Test
    @DisplayName("[성공] 좋아요 취소 후 다시 좋아요할 수 있다.")
    void 좋아요_취소_후_다시_좋아요할_수_있다() {
        teamCommandService.addTeamLike(member.getId(), notVotingTeam.getId());
        teamCommandService.removeTeamLike(member.getId(), notVotingTeam.getId());

        teamCommandService.addTeamLike(member.getId(), notVotingTeam.getId());

        assertThat(teamLikeRepository.existsByMemberIdAndTeam(member.getId(), notVotingTeam)).isTrue();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에는 좋아요할 수 없다.")
    void 존재하지_않는_팀에는_좋아요할_수_없다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> teamCommandService.addTeamLike(member.getId(), invalidTeamId))
                .isInstanceOf(TeamException.class)
                .hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 기간에는 좋아요를 등록할 수 없다.")
    void 투표_기간에는_좋아요를_등록할_수_없다() {
        final Contest votingContest = ContestFixture.createContest();
        votingContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        final Contest savedVotingContest = contestRepository.save(votingContest);

        final Team votingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(savedVotingContest.getId()));

        assertThatThrownBy(() -> teamCommandService.addTeamLike(member.getId(), votingTeam.getId()))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_DURING_VOTING_PERIOD.errorMessage());
    }

    @Test
    @DisplayName("[실패] 투표 기간에는 좋아요를 취소할 수 없다.")
    void 투표_기간에는_좋아요를_취소할_수_없다() {
        final Contest votingContest = ContestFixture.createContest();
        votingContest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        final Contest savedVotingContest = contestRepository.save(votingContest);

        final Team votingTeam = teamRepository.save(TeamFixture.createTeamWithContestId(savedVotingContest.getId()));

        assertThatThrownBy(() -> teamCommandService.removeTeamLike(member.getId(), votingTeam.getId()))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_DURING_VOTING_PERIOD.errorMessage());
    }

    @Test
    @DisplayName("[성공] 관리자는 팀 정보를 수정할 수 있다.")
    void 관리자는_팀_정보를_수정할_수_있다() {
        // given
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(votingContest));
        final Member admin = memberRepository.save(Member.generalMember()
                .name("관리자")
                .email("admin@pusan.ac.kr")
                .password("{noop}12345678")
                .studentId("000000000")
                .roles(Set.of(MemberRoleType.ROLE_관리자))
                .build());

        final TeamUpdateRequest request = new TeamUpdateRequest(
                votingContest.getId(), track.getId(), "수정된 프로젝트", "수정된 팀", "수정된 교수",
                "https://github.com/new", "https://youtube.com/new", "https://prod.new", "수정된 개요"
        );

        // when
        teamCommandService.updateTeam(admin, generalTeam.getId(), request);

        // then
        final Team updatedTeam = teamRepository.findById(generalTeam.getId()).get();
        assertThat(updatedTeam.getProjectName()).isEqualTo("수정된 프로젝트");
        assertThat(updatedTeam.getTeamName()).isEqualTo("수정된 팀");
        assertThat(updatedTeam.getContestId()).isEqualTo(votingContest.getId());
        assertThat(updatedTeam.getTrackId()).isEqualTo(track.getId());
    }

    @Test
    @DisplayName("[성공] 팀원은 템플릿의 필수 항목을 모두 포함하여 팀 정보를 수정할 수 있다.")
    void 팀원은_템플릿의_필수_항목을_모두_포함하여_팀_정보를_수정할_수_있다() {
        // given
        contestTemplateRepository.save(ContestTemplateFixture.createContestTemplate(votingContest));
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(votingContest));
        final Team team = teamRepository.save(Team.builder()
                .contestId(votingContest.getId())
                .trackId(track.getId())
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .build());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀원))
                .build());

        final TeamUpdateRequest request = new TeamUpdateRequest(
                votingContest.getId(), track.getId(), "프로젝트", "팀명", "교수명",
                "https://github.com/path", "https://youtube.com/path", "https://production.path", "개요"
        );

        // when
        teamCommandService.updateTeam(member, team.getId(), request);

        // then
        final Team updatedTeam = teamRepository.findById(team.getId()).get();
        assertThat(updatedTeam.getIsSubmitted()).isTrue();
    }

    @Test
    @DisplayName("[실패] 팀원이 템플릿의 필수 항목을 누락하면 예외가 발생한다.")
    void 팀원이_템플릿의_필수_항목을_누락하면_예외가_발생한다() {
        // given
        contestTemplateRepository.save(ContestTemplateFixture.createContestTemplate(votingContest));
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(votingContest));
        final Team team = teamRepository.save(Team.builder()
                .contestId(votingContest.getId())
                .trackId(track.getId())
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .build());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀원))
                .build());

        final TeamUpdateRequest request = new TeamUpdateRequest(
                votingContest.getId(), track.getId(), null, "팀명", "교수명",
                "https://github.com/path", "https://youtube.com/path", "https://production.path", "개요"
        );

        // when & then
        assertThatThrownBy(() -> teamCommandService.updateTeam(member, team.getId(), request))
                .isInstanceOf(TeamException.class)
                .hasMessage(TeamExceptionType.REQUIRED_FIELD_MISSING.errorMessage());
    }

    @Test
    @DisplayName("[실패] 팀원이 대회 정보를 변경하려고 하면 예외가 발생한다.")
    void 팀원이_대회_정보를_변경하려고_하면_예외가_발생한다() {
        // given
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(votingContest));
        final Team team = teamRepository.save(Team.builder()
                .contestId(votingContest.getId())
                .trackId(track.getId())
                .itemOrder(1)
                .teamMembers(new ArrayList<>())
                .build());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀원))
                .build());

        final Long otherContestId = notVotingContest.getId();
        final TeamUpdateRequest request = new TeamUpdateRequest(
                otherContestId, null, null, null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> teamCommandService.updateTeam(member, team.getId(), request))
                .isInstanceOf(TeamException.class)
                .hasMessage(TeamExceptionType.FORBIDDEN_CONTEST_OR_TRACK_UPDATE.errorMessage());
    }

    @Test
    @DisplayName("[실패] 팀에 속하지 않은 회원이 팀 정보를 수정하려고 하면 예외가 발생한다.")
    void 팀에_속하지_않은_회원이_팀_정보를_수정하려고_하면_예외가_발생한다() {
        // given
        final Team team = teamRepository.save(TeamFixture.createTeamWithContestId(votingContest.getId()));
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(99));

        final TeamUpdateRequest request = new TeamUpdateRequest(
                null, null, "프로젝트", "팀명", "교수명",
                "https://github.com/path", "https://youtube.com/path", "https://production.path", "개요"
        );

        // when & then
        assertThatThrownBy(() -> teamCommandService.updateTeam(otherMember, team.getId(), request))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }
}
