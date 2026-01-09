package com.opus.opus.team.application;

import static com.opus.opus.modules.team.exception.TeamExceptionType.NOT_FOUND_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.TeamCommentQueryService;
import com.opus.opus.modules.team.application.dto.response.TeamCommentResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamComment;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.team.TeamCommentFixture;
import com.opus.opus.team.TeamFixture;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TeamCommentQueryServiceTest extends IntegrationTest {

    @Autowired
    private TeamCommentQueryService teamCommentQueryService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TeamCommentRepository teamCommentRepository;

    private Team team;
    private Member member;
    private final String commentDescription = "테스트용 댓글입니다.";

    @BeforeEach
    void setUp() {
        team = teamRepository.save(TeamFixture.createTeam());
        member = memberRepository.save(MemberFixture.createMember());
    }

    @Test
    @DisplayName("[성공] 팀의 댓글 목록을 조회할 수 있다.")
    void 팀의_댓글_목록을_조회할_수_있다() {
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId());

        assertThat(commentResponseList).hasSize(2);
        assertThat(commentResponseList.get(0).description()).isEqualTo(commentDescription);
        assertThat(commentResponseList.get(0).memberId()).isEqualTo(member.getId());
        assertThat(commentResponseList.get(0).memberName()).isEqualTo(member.getName());
        assertThat(commentResponseList.get(0).teamId()).isEqualTo(team.getId());
    }

    @Test
    @DisplayName("[성공] 댓글이 없는 팀의 경우 빈 리스트를 반환한다.")
    void 댓글이_없는_팀의_경우_빈_리스트를_반환한다() {
        final List<TeamCommentResponse> responses = teamCommentQueryService.getComments(team.getId());

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("[성공] 댓글 목록은 최신순으로 정렬되어 조회된다.")
    void 댓글_목록은_최신순으로_정렬되어_조회된다() {
        final TeamComment firstComment = teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        final TeamComment secondComment = teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId());

        assertThat(commentResponseList).hasSize(2);
        assertThat(commentResponseList.get(0).commentId()).isEqualTo(secondComment.getId());
        assertThat(commentResponseList.get(1).commentId()).isEqualTo(firstComment.getId());
    }

    @Test
    @DisplayName("[성공] 여러 회원이 작성한 댓글을 조회할 수 있다.")
    void 여러_회원이_작성한_댓글을_조회할_수_있다() {
        final Member otherMember = memberRepository.save(MemberFixture.createMember(1));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, otherMember.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId());

        assertThat(commentResponseList).hasSize(2);
        assertThat(commentResponseList).extracting(TeamCommentResponse::memberName)
                .containsExactly(otherMember.getName(), member.getName());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀의 댓글 목록은 조회할 수 없다.")
    void 존재하지_않는_팀의_댓글_목록은_조회할_수_없다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> {
            teamCommentQueryService.getComments(invalidTeamId);
        }).isInstanceOf(TeamException.class).hasMessage(NOT_FOUND_TEAM.errorMessage());
    }
}
