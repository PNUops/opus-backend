package com.opus.opus.member.application;

import static com.opus.opus.modules.member.exception.MemberExceptionType.NOT_FOUND_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestAwardRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.member.application.MemberQueryService;
import com.opus.opus.modules.member.application.dto.response.EmailFindResponse;
import com.opus.opus.modules.member.application.dto.response.MyProjectResponse;
import com.opus.opus.modules.member.application.dto.response.MyVoteResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

    private Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createMember());
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
}
