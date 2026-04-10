package com.opus.opus.member.application;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_ORDER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_RANGE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import com.opus.opus.modules.member.application.MemberQueryService;
import com.opus.opus.modules.member.application.dto.response.AccountInfoResponse;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyProjectResponse;
import com.opus.opus.modules.member.domain.dao.MyVoteResponse;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikePreviewResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikedProjectResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamVoteFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.file.FileFixture;
import org.antlr.v4.runtime.misc.Pair;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.team.TeamCommentFixture;
import com.opus.opus.team.TeamFixture;
import com.opus.opus.team.TeamLikeFixture;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;

public class MemberQueryServiceTest extends IntegrationTest {

    @Autowired
    private MemberQueryService memberQueryService;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private ContestAwardRepository contestAwardRepository;
    @Autowired
    private TeamContestAwardRepository teamContestAwardRepository;
    @Autowired
    private TeamVoteRepository teamVoteRepository;
    @Autowired
    private FileRepository fileRepository;
    @Autowired
    private ContestCategoryRepository contestCategoryRepository;
    @Autowired
    private TeamCommentRepository teamCommentRepository;
    @Autowired
    private TeamLikeRepository teamLikeRepository;

    @Autowired
    private FileStorageUtil fileStorageUtil;

    private Member member;
    private Team team;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createMember());

        final ContestCategory category = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        final Contest contest = contestRepository.save(ContestFixture.createContestWithCategoryId(category.getId()));
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
    }

    @Test
    @DisplayName("[성공] 가입된 회원은 이메일 찾기를 할 수 있다.")
    void 가입된_회원은_이메일_찾기를_할_수_있다() {
        final EmailFindResponse response = memberQueryService.getMyEmail(member.getStudentId());

        assertThat(response.email()).isEqualTo(member.getEmail());
    }

    @Test
    @DisplayName("[실패] 미가입 회원은 이메일 찾기가 불가하다.")
    void 미가입_회원은_이메일_찾기가_불가하다() {
        final String notExistMemberEmail = "qwqw@pusan.ac.kr";

        assertThatThrownBy(() -> {
            memberQueryService.getMyEmail(notExistMemberEmail);
        }).isInstanceOf(MemberException.class).hasMessage(NOT_FOUND_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 프로필 이미지가 있으면 이미지 응답이 반환된다.")
    void 프로필_이미지가_있으면_이미지_응답이_반환된다() {
        // given
        final File savedFile = fileRepository.save(FileFixture.createMemberProfileFile(member.getId()));
        savedFile.updateIsWebpConverted(true);
        fileRepository.saveAndFlush(savedFile);

        final Resource resource = new ByteArrayResource("content".getBytes());
        given(fileStorageUtil.findFileAndType(savedFile.getId()))
                .willReturn(new Pair<>(resource, "image/webp"));

        // when
        final ImageResponse response = memberQueryService.getProfileImage(member);

        // then
        assertThat(response).isNotNull();
        assertThat(response.contentType()).isEqualTo("image/webp");
    }

    @Test
    @DisplayName("[실패] 프로필 이미지가 없으면 예외가 발생한다.")
    void 프로필_이미지가_없으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> memberQueryService.getProfileImage(member))
                .isInstanceOf(FileException.class)
                .hasMessage(NOT_EXISTS_MATCHING_IMAGE_ID.errorMessage());
    }

    @Test
    @DisplayName("[성공] 참여한 프로젝트가 없으면 빈 리스트를 반환한다.")
    void 참여한_프로젝트가_없으면_빈_리스트를_반환한다() {
        final List<MyProjectResponse> responses = memberQueryService.getMyProjects(member.getId());

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("[성공] 참여한 프로젝트 목록을 조회할 수 있다.")
    void 참여한_프로젝트_목록을_조회할_수_있다() {
        final Contest contest = contestRepository.save(ContestFixture.createContest());
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final Team team = teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        saveTeamMember(team);

        final List<MyProjectResponse> responses = memberQueryService.getMyProjects(member.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).contestId()).isEqualTo(contest.getId());
        assertThat(responses.get(0).contestName()).isEqualTo(contest.getContestName());
        assertThat(responses.get(0).teamId()).isEqualTo(team.getId());
        assertThat(responses.get(0).teamName()).isEqualTo(team.getTeamName());
        assertThat(responses.get(0).projectName()).isEqualTo(team.getProjectName());
        assertThat(responses.get(0).trackName()).isEqualTo(track.getTrackName());
    }

    @Test
    @DisplayName("[성공] 수상 정보가 포함된 프로젝트를 조회할 수 있다.")
    void 수상_정보가_포함된_프로젝트를_조회할_수_있다() {
        final Contest contest = contestRepository.save(ContestFixture.createContest());
        final Team team = teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), null));
        saveTeamMember(team);
        final ContestAward award = contestAwardRepository.save(ContestAward.builder().contest(contest).awardName("대상").awardColor("#FF0000").build());
        teamContestAwardRepository.save(TeamContestAward.builder().team(team).contestAwardId(award.getId()).build());

        final List<MyProjectResponse> responses = memberQueryService.getMyProjects(member.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).awards()).hasSize(1);
        assertThat(responses.get(0).awards().get(0).awardName()).isEqualTo("대상");
        assertThat(responses.get(0).awards().get(0).awardColor()).isEqualTo("#FF0000");
    }

    @Test
    @DisplayName("[성공] 여러 대회에 참여한 경우 모든 프로젝트를 조회할 수 있다.")
    void 여러_대회에_참여한_경우_모든_프로젝트를_조회할_수_있다() {
        final Contest contest1 = contestRepository.save(ContestFixture.createContest());
        final Contest contest2 = contestRepository.save(ContestFixture.createContestWithCategoryId(2L));
        final Team team1 = teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest1.getId(), null));
        final Team team2 = teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest2.getId(), null));
        saveTeamMember(team1);
        saveTeamMember(team2);

        final List<MyProjectResponse> responses = memberQueryService.getMyProjects(member.getId());

        assertThat(responses).hasSize(2);
    }

    @Test
    @DisplayName("[성공] 투표 기간인 대회에서 투표한 프로젝트를 조회할 수 있다.")
    void 투표_기간인_대회에서_투표한_프로젝트를_조회할_수_있다() {
        final Contest contest = contestRepository.save(ContestFixture.createContest());
        contest.updateVotePeriod(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        final Team team = teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), null));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        final List<MyVoteResponse> responses = memberQueryService.getMyVotes(member.getId());

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).contestId()).isEqualTo(contest.getId());
        assertThat(responses.get(0).contestName()).isEqualTo(contest.getContestName());
        assertThat(responses.get(0).teamId()).isEqualTo(team.getId());
        assertThat(responses.get(0).teamName()).isEqualTo(team.getTeamName());
        assertThat(responses.get(0).projectName()).isEqualTo(team.getProjectName());
    }

    @Test
    @DisplayName("[성공] 투표 기간이 아닌 대회의 투표는 조회되지 않는다.")
    void 투표_기간이_아닌_대회의_투표는_조회되지_않는다() {
        final Contest contest = contestRepository.save(ContestFixture.createContest());
        contest.updateVotePeriod(LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(1));
        final Team team = teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), null));
        teamVoteRepository.save(TeamVoteFixture.createTeamVote(team, member.getId(), true));

        final List<MyVoteResponse> responses = memberQueryService.getMyVotes(member.getId());

        assertThat(responses).isEmpty();
    }

    private void saveTeamMember(final Team team) {
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀원))
                .build());
    }

    @Test
    @DisplayName("[성공] 로그인한 회원은 계정 정보를 조회할 수 있다.")
    void 로그인한_회원은_계정_정보를_조회할_수_있다() {
        final AccountInfoResponse response = memberQueryService.getAccountInfo(member.getId());

        assertThat(response.name()).isEqualTo(member.getName());
        assertThat(response.email()).isEqualTo(member.getEmail());
        assertThat(response.githubUrl()).isNull();
        assertThat(response.isProfilePublic()).isTrue();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 회원의 계정 정보는 조회할 수 없다.")
    void 존재하지_않는_회원의_계정_정보는_조회할_수_없다() {
        assertThatThrownBy(() -> {
            memberQueryService.getAccountInfo(999L);
        }).isInstanceOf(MemberException.class).hasMessage(NOT_FOUND_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 나의 댓글 목록을 조회할 수 있다.")
    void 나의_댓글_목록을_조회할_수_있다() {
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));

        final Page<MyCommentResponse> result = memberQueryService.getMyComments(member.getId(),
                "latest", null, null, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).comment().memberName()).isEqualTo(member.getName());
    }

    @Test
    @DisplayName("[성공] 나의 댓글 목록을 날짜 필터로 조회할 수 있다.")
    void 나의_댓글_목록을_날짜_필터로_조회할_수_있다() {
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));

        final Page<MyCommentResponse> result = memberQueryService.getMyComments(member.getId(),
                "latest", LocalDate.now().minusDays(1), LocalDate.now(), 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 나의 댓글 목록을 오래된 순으로 정렬할 수 있다.")
    void 나의_댓글_목록을_오래된_순으로_정렬할_수_있다() {
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));

        final Page<MyCommentResponse> result = memberQueryService.getMyComments(member.getId(),
                "oldest", null, null, 0, 10);

        assertThat(result.getContent().get(0).comment().createdAt())
                .isBeforeOrEqualTo(result.getContent().get(1).comment().createdAt());
    }

    @Test
    @DisplayName("[실패] 날짜 범위가 불완전하면 예외가 발생한다.")
    void 날짜_범위가_불완전하면_예외가_발생한다() {
        assertThatThrownBy(() -> {
            memberQueryService.getMyComments(member.getId(), "latest", LocalDate.now(), null, 0, 10);
        }).isInstanceOf(MemberException.class).hasMessage(INVALID_DATE_RANGE.errorMessage());
    }

    @Test
    @DisplayName("[실패] startDate가 endDate보다 뒤에 있으면 예외가 발생한다.")
    void startDate가_endDate보다_뒤에_있으면_예외가_발생한다() {
        assertThatThrownBy(() -> {
            memberQueryService.getMyComments(member.getId(), "latest",
                    LocalDate.now(), LocalDate.now().minusDays(1), 0, 10);
        }).isInstanceOf(MemberException.class).hasMessage(INVALID_DATE_ORDER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 최근 좋아요한 프로젝트 미리보기를 조회할 수 있다.")
    void 최근_좋아요한_프로젝트_미리보기를_조회할_수_있다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), true));

        final List<MyLikePreviewResponse> result = memberQueryService.getMyLikePreview(member.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).teamId()).isEqualTo(team.getId());
    }

    @Test
    @DisplayName("[성공] 좋아요 미리보기는 최대 3개까지 반환된다.")
    void 좋아요_미리보기는_최대_3개까지_반환된다() {
        for (int i = 0; i < 5; i++) {
            final Team newTeam = teamRepository.save(TeamFixture.createTeamWithContestId(team.getContestId()));
            teamLikeRepository.save(TeamLikeFixture.createTeamLike(newTeam, member.getId(), true));
        }

        final List<MyLikePreviewResponse> result = memberQueryService.getMyLikePreview(member.getId());

        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("[성공] 좋아요 취소한 프로젝트는 미리보기에 포함되지 않는다.")
    void 좋아요_취소한_프로젝트는_미리보기에_포함되지_않는다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), false));

        final List<MyLikePreviewResponse> result = memberQueryService.getMyLikePreview(member.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("[성공] 나의 좋아요 전체 목록을 조회할 수 있다.")
    void 나의_좋아요_전체_목록을_조회할_수_있다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), true));

        final Page<MyLikedProjectResponse> result = memberQueryService.getMyLikedProjects(member.getId(),
                "latest", null, null, null, null, 0, 12);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).teamName()).isEqualTo(team.getTeamName());
    }

    @Test
    @DisplayName("[성공] 나의 좋아요 목록을 대회 필터로 조회할 수 있다.")
    void 나의_좋아요_목록을_대회_필터로_조회할_수_있다() {
        teamLikeRepository.save(TeamLikeFixture.createTeamLike(team, member.getId(), true));

        final Page<MyLikedProjectResponse> result = memberQueryService.getMyLikedProjects(member.getId(),
                "latest", null, null, null, team.getContestId(), 0, 12);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}
