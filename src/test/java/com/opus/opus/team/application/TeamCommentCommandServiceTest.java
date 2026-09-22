package com.opus.opus.team.application;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static com.opus.opus.modules.team.domain.TeamCommentVisibility.PUBLIC;
import static com.opus.opus.modules.team.domain.TeamCommentVisibility.TEAM;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.COMMENT_LENGTH_EXCEEDED;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.COMMENT_NOT_BELONG_TO_TEAM;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_ALLOWED_TO_WRITE_TEAM_ONLY_COMMENT;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_FOUND_COMMENT;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_OWNER_COMMENT;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.TeamCommentCommandService;
import com.opus.opus.modules.team.application.dto.request.TeamCommentCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamCommentUpdateRequest;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamComment;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamCommentException;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.team.TeamFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TeamCommentCommandServiceTest extends IntegrationTest {

    @Autowired
    private TeamCommentCommandService teamCommentCommandService;

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamCommentRepository teamCommentRepository;

    private Team team;
    private Member member;
    private final String commentDescription = "테스트용 댓글입니다.";
    private final String updatedCommentDescription = "수정된 댓글입니다.";
    private TeamCommentCreateRequest commentCreateRequest;

    @BeforeEach
    void setUp() {
        team = teamRepository.save(TeamFixture.createTeam());
        member = memberRepository.save(MemberFixture.createMember());
        commentCreateRequest = new TeamCommentCreateRequest(commentDescription, PUBLIC);
    }

    @Test
    @DisplayName("[성공] 팀 댓글이 정상적으로 등록된다.")
    void 팀_댓글이_정상적으로_등록된다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());

        final TeamComment savedComment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        assertThat(savedComment.getDescription()).isEqualTo(commentCreateRequest.description());
        assertThat(savedComment.getMemberId()).isEqualTo(member.getId());
        assertThat(savedComment.getTeam().getId()).isEqualTo(team.getId());
        assertThat(savedComment.getVisibility()).isEqualTo(PUBLIC);
    }

    @Test
    @DisplayName("[성공] visibility를 지정하지 않으면 공개 댓글로 등록된다.")
    void visibility를_지정하지_않으면_공개_댓글로_등록된다() {
        teamCommentCommandService.createComment(team.getId(), member, commentDescription, null);

        final TeamComment savedComment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        assertThat(savedComment.getVisibility()).isEqualTo(PUBLIC);
    }

    @Test
    @DisplayName("[성공] 교수는 팀 피드백을 등록할 수 있다.")
    void 교수는_팀_피드백을_등록할_수_있다() {
        final Member professor = memberRepository.save(MemberFixture.createMemberWithRole("교수", 1, ROLE_교수));

        teamCommentCommandService.createComment(team.getId(), professor, commentDescription, TEAM);

        final TeamComment savedComment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        assertThat(savedComment.getMemberId()).isEqualTo(professor.getId());
        assertThat(savedComment.getVisibility()).isEqualTo(TEAM);
    }

    @Test
    @DisplayName("[성공] 외부멘토는 팀 피드백을 등록할 수 있다.")
    void 외부멘토는_팀_피드백을_등록할_수_있다() {
        final Member mentor = memberRepository.save(MemberFixture.createMemberWithRole("외부멘토", 2, ROLE_외부멘토));

        teamCommentCommandService.createComment(team.getId(), mentor, commentDescription, TEAM);

        final TeamComment savedComment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        assertThat(savedComment.getMemberId()).isEqualTo(mentor.getId());
        assertThat(savedComment.getVisibility()).isEqualTo(TEAM);
    }

    @Test
    @DisplayName("[실패] 교수 또는 외부멘토가 아닌 회원은 팀 피드백을 등록할 수 없다.")
    void 교수_또는_외부멘토가_아닌_회원은_팀_피드백을_등록할_수_없다() {
        assertThatThrownBy(() -> {
            teamCommentCommandService.createComment(team.getId(), member, commentDescription, TEAM);
        }).isInstanceOf(TeamCommentException.class).hasMessage(NOT_ALLOWED_TO_WRITE_TEAM_ONLY_COMMENT.errorMessage());

        assertThat(teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에는 댓글 등록이 불가능하다.")
    void 존재하지_않는_팀에는_댓글_등록이_불가능하다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> {
            teamCommentCommandService.createComment(invalidTeamId, member, commentCreateRequest.description(), commentCreateRequest.visibility());
        }).isInstanceOf(TeamException.class).hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 공개 댓글은 255자를 초과하면 등록할 수 없다.")
    void 공개_댓글은_255자를_초과하면_등록할_수_없다() {
        final String tooLongDescription = "a".repeat(256);

        assertThatThrownBy(() -> {
            teamCommentCommandService.createComment(team.getId(), member, tooLongDescription, PUBLIC);
        }).isInstanceOf(TeamCommentException.class)
                .hasMessage(String.format(COMMENT_LENGTH_EXCEEDED.errorMessage(), 255));

        assertThat(teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId())).isEmpty();
    }

    @Test
    @DisplayName("[성공] 팀 피드백은 3000자까지 등록할 수 있다.")
    void 팀_피드백은_3000자까지_등록할_수_있다() {
        final Member professor = memberRepository.save(MemberFixture.createMemberWithRole("교수", 1, ROLE_교수));
        final String maxLengthDescription = "a".repeat(3000);

        teamCommentCommandService.createComment(team.getId(), professor, maxLengthDescription, TEAM);

        final TeamComment savedComment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        assertThat(savedComment.getDescription()).hasSize(3000);
    }

    @Test
    @DisplayName("[실패] 팀 피드백은 3000자를 초과하면 등록할 수 없다.")
    void 팀_피드백은_3000자를_초과하면_등록할_수_없다() {
        final Member professor = memberRepository.save(MemberFixture.createMemberWithRole("교수", 1, ROLE_교수));
        final String tooLongDescription = "a".repeat(3001);

        assertThatThrownBy(() -> {
            teamCommentCommandService.createComment(team.getId(), professor, tooLongDescription, TEAM);
        }).isInstanceOf(TeamCommentException.class)
                .hasMessage(String.format(COMMENT_LENGTH_EXCEEDED.errorMessage(), 3000));

        assertThat(teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId())).isEmpty();
    }

    @Test
    @DisplayName("[성공] 댓글이 정상적으로 수정된다.")
    void 댓글이_정상적으로_수정된다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final TeamCommentUpdateRequest updateRequest = new TeamCommentUpdateRequest(updatedCommentDescription);

        teamCommentCommandService.updateComment(team.getId(), comment.getId(), member.getId(), updateRequest.description());

        final TeamComment updatedComment = teamCommentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updatedComment.getDescription()).isEqualTo(updateRequest.description());
        assertThat(updatedComment.getDescription()).isNotEqualTo(commentCreateRequest.description());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 댓글은 수정할 수 없다.")
    void 존재하지_않는_댓글은_수정할_수_없다() {
        final Long invalidCommentId = 999L;
        final TeamCommentUpdateRequest request = new TeamCommentUpdateRequest(updatedCommentDescription);

        assertThatThrownBy(() -> {
            teamCommentCommandService.updateComment(team.getId(), invalidCommentId, member.getId(), request.description());
        }).isInstanceOf(TeamCommentException.class).hasMessage(NOT_FOUND_COMMENT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 댓글은 수정할 수 없다.")
    void 본인이_작성하지_않은_댓글은_수정할_수_없다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));

        final TeamCommentUpdateRequest updateRequest = new TeamCommentUpdateRequest(updatedCommentDescription);

        assertThatThrownBy(() -> {
            teamCommentCommandService.updateComment(team.getId(), comment.getId(), otherMember.getId(), updateRequest.description());
        }).isInstanceOf(TeamCommentException.class).hasMessage(NOT_OWNER_COMMENT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 팀의 댓글은 수정할 수 없다.")
    void 다른_팀의_댓글은_수정할_수_없다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final Team otherTeam = teamRepository.save(TeamFixture.createTeam());

        final TeamCommentUpdateRequest updateRequest = new TeamCommentUpdateRequest(updatedCommentDescription);

        assertThatThrownBy(() -> {
            teamCommentCommandService.updateComment(otherTeam.getId(), comment.getId(), member.getId(), updateRequest.description());
        }).isInstanceOf(TeamCommentException.class)
                .hasMessage(COMMENT_NOT_BELONG_TO_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 공개 댓글은 255자를 초과하는 내용으로 수정할 수 없다.")
    void 공개_댓글은_255자를_초과하는_내용으로_수정할_수_없다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final String tooLongDescription = "a".repeat(256);

        assertThatThrownBy(() -> {
            teamCommentCommandService.updateComment(team.getId(), comment.getId(), member.getId(), tooLongDescription);
        }).isInstanceOf(TeamCommentException.class)
                .hasMessage(String.format(COMMENT_LENGTH_EXCEEDED.errorMessage(), 255));

        final TeamComment unchangedComment = teamCommentRepository.findById(comment.getId()).orElseThrow();
        assertThat(unchangedComment.getDescription()).isEqualTo(commentCreateRequest.description());
    }

    @Test
    @DisplayName("[성공] 댓글이 정상적으로 삭제된다.")
    void 댓글이_정상적으로_삭제된다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);

        teamCommentCommandService.deleteComment(team.getId(), comment.getId(), member.getId());

        assertThat(teamCommentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] 다른 팀의 댓글은 삭제할 수 없다.")
    void 다른_팀의_댓글은_삭제할_수_없다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final Team otherTeam = teamRepository.save(TeamFixture.createTeam());

        assertThatThrownBy(() -> {
            teamCommentCommandService.deleteComment(otherTeam.getId(), comment.getId(), member.getId());
        }).isInstanceOf(TeamCommentException.class)
                .hasMessage(COMMENT_NOT_BELONG_TO_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 댓글은 삭제할 수 없다.")
    void 존재하지_않는_댓글은_삭제할_수_없다() {
        final Long invalidCommentId = 999L;

        assertThatThrownBy(() -> {
            teamCommentCommandService.deleteComment(team.getId(), invalidCommentId, member.getId());
        }).isInstanceOf(TeamCommentException.class).hasMessage(NOT_FOUND_COMMENT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 본인이 작성하지 않은 댓글은 삭제할 수 없다.")
    void 본인이_작성하지_않은_댓글은_삭제할_수_없다() {
        teamCommentCommandService.createComment(team.getId(), member, commentCreateRequest.description(), commentCreateRequest.visibility());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));

        assertThatThrownBy(() -> {
            teamCommentCommandService.deleteComment(team.getId(), comment.getId(), otherMember.getId());
        }).isInstanceOf(TeamCommentException.class).hasMessage(NOT_OWNER_COMMENT.errorMessage());
    }
}
