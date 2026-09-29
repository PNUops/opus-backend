package com.opus.opus.team.application;

import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.team.domain.TeamCommentVisibility.PUBLIC;
import static com.opus.opus.modules.team.domain.TeamCommentVisibility.TEAM;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamCommentExceptionType.INVALID_COMMENT_VISIBILITY;
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
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.dao.TeamCommentRepository;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamCommentException;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.team.TeamCommentFixture;
import com.opus.opus.team.TeamFixture;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Team team;
    private Member member;
    private Member professor;
    private final String commentDescription = "테스트용 댓글입니다.";

    @BeforeEach
    void setUp() {
        team = teamRepository.save(TeamFixture.createTeam());
        member = memberRepository.save(MemberFixture.createMember());
        professor = memberRepository.save(MemberFixture.createMemberWithRole("교수", 8, ROLE_교수));
    }

    @Test
    @DisplayName("[성공] 팀의 댓글 목록을 조회할 수 있다.")
    void 팀의_댓글_목록을_조회할_수_있다() {
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId(), member, null);

        assertThat(commentResponseList).hasSize(2);
        assertThat(commentResponseList.get(0).description()).isEqualTo(commentDescription);
        assertThat(commentResponseList.get(0).memberId()).isEqualTo(member.getId());
        assertThat(commentResponseList.get(0).memberName()).isEqualTo(member.getName());
        assertThat(commentResponseList.get(0).teamId()).isEqualTo(team.getId());
        assertThat(commentResponseList.get(0).visibility()).isEqualTo(PUBLIC);
        assertThat(commentResponseList.get(0).memberRoleType()).isNull();
        assertThat(commentResponseList.get(0).createdAt()).isNotNull();
        assertThat(commentResponseList.get(0).updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("[성공] 댓글이 없는 팀의 경우 빈 리스트를 반환한다.")
    void 댓글이_없는_팀의_경우_빈_리스트를_반환한다() {
        final List<TeamCommentResponse> responses = teamCommentQueryService.getComments(team.getId(), member, null);

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("[성공] 댓글 목록은 최신순으로 정렬되어 조회된다.")
    void 댓글_목록은_최신순으로_정렬되어_조회된다() {
        final TeamComment firstComment = teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        final TeamComment secondComment = teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId(), member, null);

        assertThat(commentResponseList).hasSize(2);
        assertThat(commentResponseList.get(0).commentId()).isEqualTo(secondComment.getId());
        assertThat(commentResponseList.get(1).commentId()).isEqualTo(firstComment.getId());
    }

    @Test
    @DisplayName("[성공] 여러 회원이 작성한 댓글을 조회할 수 있다.")
    void 여러_회원이_작성한_댓글을_조회할_수_있다() {
        final Member otherMember = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, otherMember.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId(), member, null);

        assertThat(commentResponseList).hasSize(2);
        assertThat(commentResponseList).extracting(TeamCommentResponse::memberName)
                .containsExactly(otherMember.getName(), member.getName());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀의 댓글 목록은 조회할 수 없다.")
    void 존재하지_않는_팀의_댓글_목록은_조회할_수_없다() {
        final Long invalidTeamId = 999L;

        assertThatThrownBy(() -> {
            teamCommentQueryService.getComments(invalidTeamId, member, null);
        }).isInstanceOf(TeamException.class).hasMessage(NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 팀 구성원은 팀 피드백을 조회할 수 있다.")
    void 팀_구성원은_팀_피드백을_조회할_수_있다() {
        saveTeamMember(team, member);
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, professor.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId(), member, null);

        assertThat(commentResponseList).hasSize(2);
        assertThat(commentResponseList).extracting(TeamCommentResponse::visibility)
                .containsExactly(TEAM, PUBLIC);
        assertThat(commentResponseList.get(0).memberRoleType()).isEqualTo(ROLE_교수.name());
    }

    @Test
    @DisplayName("[성공] 관리자는 팀 피드백을 조회할 수 있다.")
    void 관리자는_팀_피드백을_조회할_수_있다() {
        final Member admin = memberRepository.save(MemberFixture.createMemberWithRole("관리자", 9, ROLE_관리자));
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, professor.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId(), admin, null);

        assertThat(commentResponseList).hasSize(1);
        assertThat(commentResponseList.get(0).visibility()).isEqualTo(TEAM);
    }

    @Test
    @DisplayName("[성공] 작성자 본인은 자신의 팀 피드백을 조회할 수 있다.")
    void 작성자_본인은_자신의_팀_피드백을_조회할_수_있다() {
        final Member otherProfessor = memberRepository.save(MemberFixture.createMemberWithRole("다른교수", 7, ROLE_교수));
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, professor.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, otherProfessor.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId(), professor, null);

        assertThat(commentResponseList).hasSize(1);
        assertThat(commentResponseList.get(0).memberId()).isEqualTo(professor.getId());
    }

    @Test
    @DisplayName("[성공] 팀 구성원이 아닌 회원에게는 팀 피드백이 노출되지 않는다.")
    void 팀_구성원이_아닌_회원에게는_팀_피드백이_노출되지_않는다() {
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, professor.getId()));

        final List<TeamCommentResponse> commentResponseList = teamCommentQueryService.getComments(team.getId(), member, null);

        assertThat(commentResponseList).hasSize(1);
        assertThat(commentResponseList.get(0).visibility()).isEqualTo(PUBLIC);
    }


    @Test
    @DisplayName("[성공] visibility를 TEAM으로 지정하면 팀 피드백만 조회된다.")
    void visibility를_TEAM으로_지정하면_팀_피드백만_조회된다() {
        saveTeamMember(team, member);
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, professor.getId()));

        final List<TeamCommentResponse> commentResponseList =
                teamCommentQueryService.getComments(team.getId(), member, TEAM.name());

        assertThat(commentResponseList).hasSize(1);
        assertThat(commentResponseList.get(0).visibility()).isEqualTo(TEAM);
    }

    @Test
    @DisplayName("[성공] visibility를 PUBLIC으로 지정하면 공개 댓글만 조회된다.")
    void visibility를_PUBLIC으로_지정하면_공개_댓글만_조회된다() {
        saveTeamMember(team, member);
        teamCommentRepository.save(TeamCommentFixture.createTeamComment(team, member.getId()));
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, professor.getId()));

        final List<TeamCommentResponse> commentResponseList =
                teamCommentQueryService.getComments(team.getId(), member, PUBLIC.name());

        assertThat(commentResponseList).hasSize(1);
        assertThat(commentResponseList.get(0).visibility()).isEqualTo(PUBLIC);
    }

    @Test
    @DisplayName("[성공] 팀 구성원이 아닌 회원이 visibility를 TEAM으로 지정하면 빈 리스트를 반환한다.")
    void 팀_구성원이_아닌_회원이_visibility를_TEAM으로_지정하면_빈_리스트를_반환한다() {
        teamCommentRepository.save(TeamCommentFixture.createTeamOnlyComment(team, professor.getId()));

        final List<TeamCommentResponse> commentResponseList =
                teamCommentQueryService.getComments(team.getId(), member, TEAM.name());

        assertThat(commentResponseList).isEmpty();
    }

    @Test
    @DisplayName("[실패] 유효하지 않은 visibility 값으로는 조회할 수 없다.")
    void 유효하지_않은_visibility_값으로는_조회할_수_없다() {
        assertThatThrownBy(() -> {
            teamCommentQueryService.getComments(team.getId(), member, "SECRET");
        }).isInstanceOf(TeamCommentException.class).hasMessage(INVALID_COMMENT_VISIBILITY.errorMessage());
    }

    private void saveTeamMember(final Team team, final Member member) {
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(ROLE_팀원))
                .build());
    }
}
