package com.opus.opus.team.application;

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

import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.COMMENT_NOT_BELONG_TO_TEAM;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_FOUND_COMMENT;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.NOT_OWNER_COMMENT;
import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        commentCreateRequest = new TeamCommentCreateRequest(commentDescription);
    }

    @Test
    @DisplayName("[성공] 팀 댓글이 정상적으로 등록된다.")
    void 팀_댓글이_정상적으로_등록된다() {
        teamCommentCommandService.createComment(team.getId(), member.getId(), commentCreateRequest.description());

        final TeamComment savedComment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        assertThat(savedComment.getDescription()).isEqualTo(commentCreateRequest.description());
        assertThat(savedComment.getMemberId()).isEqualTo(member.getId());
        assertThat(savedComment.getTeam().getId()).isEqualTo(team.getId());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀에는 댓글 등록이 불가능하다.")
    void 존재하지_않는_팀에는_댓글_등록이_불가능하다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> {
            teamCommentCommandService.createComment(invalidTeamId, member.getId(), commentCreateRequest.description());
        }).isInstanceOf(TeamException.class).hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 댓글이 정상적으로 수정된다.")
    void 댓글이_정상적으로_수정된다() {
        teamCommentCommandService.createComment(team.getId(), member.getId(), commentCreateRequest.description());
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
        teamCommentCommandService.createComment(team.getId(), member.getId(), commentCreateRequest.description());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final Member otherMember = memberRepository.save(MemberFixture.createMember(1));

        final TeamCommentUpdateRequest updateRequest = new TeamCommentUpdateRequest(updatedCommentDescription);

        assertThatThrownBy(() -> {
            teamCommentCommandService.updateComment(team.getId(), comment.getId(), otherMember.getId(), updateRequest.description());
        }).isInstanceOf(TeamCommentException.class).hasMessage(NOT_OWNER_COMMENT.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 팀의 댓글은 수정할 수 없다.")
    void 다른_팀의_댓글은_수정할_수_없다() {
        teamCommentCommandService.createComment(team.getId(), member.getId(), commentCreateRequest.description());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final Team otherTeam = teamRepository.save(TeamFixture.createTeam());

        final TeamCommentUpdateRequest updateRequest = new TeamCommentUpdateRequest(updatedCommentDescription);

        assertThatThrownBy(() -> {
            teamCommentCommandService.updateComment(otherTeam.getId(), comment.getId(), member.getId(), updateRequest.description());
        }).isInstanceOf(TeamCommentException.class)
                .hasMessage(COMMENT_NOT_BELONG_TO_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 댓글이 정상적으로 삭제된다.")
    void 댓글이_정상적으로_삭제된다() {
        teamCommentCommandService.createComment(team.getId(), member.getId(), commentCreateRequest.description());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);

        teamCommentCommandService.deleteComment(team.getId(), comment.getId(), member.getId());

        assertThat(teamCommentRepository.findById(comment.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] 다른 팀의 댓글은 삭제할 수 없다.")
    void 다른_팀의_댓글은_삭제할_수_없다() {
        teamCommentCommandService.createComment(team.getId(), member.getId(), commentCreateRequest.description());
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
        teamCommentCommandService.createComment(team.getId(), member.getId(), commentCreateRequest.description());
        final TeamComment comment = teamCommentRepository.findAllByTeamIdOrderByIdDesc(team.getId()).get(0);
        final Member otherMember = memberRepository.save(MemberFixture.createMember(1));

        assertThatThrownBy(() -> {
            teamCommentCommandService.deleteComment(team.getId(), comment.getId(), otherMember.getId());
        }).isInstanceOf(TeamCommentException.class).hasMessage(NOT_OWNER_COMMENT.errorMessage());
    }
}
