package com.opus.opus.member.application;

import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_ORDER;
import static com.opus.opus.modules.member.exception.MemberExceptionType.INVALID_DATE_RANGE;
import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.member.application.MemberQueryService;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyCommentResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikePreviewResponse;
import com.opus.opus.modules.member.application.dto.response.MyLikedProjectResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.member.exception.MemberException;
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
import org.springframework.data.domain.Page;

public class MemberQueryServiceTest extends IntegrationTest {

    @Autowired
    private MemberQueryService memberQueryService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ContestCategoryRepository contestCategoryRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private ContestTrackRepository contestTrackRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamCommentRepository teamCommentRepository;

    @Autowired
    private TeamLikeRepository teamLikeRepository;

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
