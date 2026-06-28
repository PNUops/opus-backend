package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionDetailResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionStatus;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.modules.team.exception.TeamMemberExceptionType;
import com.opus.opus.team.TeamFixture;
import java.time.LocalDateTime;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ContestSubmissionQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionQueryService contestSubmissionQueryService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private ContestSubmissionItemRepository contestSubmissionItemRepository;
    @Autowired
    private ContestSubmissionRepository contestSubmissionRepository;
    @Autowired
    private FileDocumentRepository fileDocumentRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Contest contest;
    private Team team;
    private Member member;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
    }

    private void joinTeam(final Member loginMember, final Team joinTeam) {
        teamMemberRepository.save(TeamMember.builder()
                .memberId(loginMember.getId())
                .team(joinTeam)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀장))
                .build());
    }

    @Test
    @DisplayName("[성공] 팀원이 제출물 상세를 조회하면 기본 정보와 파일 목록을 반환한다.")
    void 제출물_상세를_조회한다() {
        joinTeam(member, team);
        final ContestTrack track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest, track));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), item));
        final File file = File.create("발표자료.pdf", "files/2026-06-24/a.pdf", "application/pdf", 1048576L);
        fileDocumentRepository.save(FileDocument.builder()
                .file(file).submissionId(submission.getId()).fileOrder(1).build());

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
        assertThat(response.teamId()).isEqualTo(team.getId());
        assertThat(response.teamName()).isEqualTo(team.getTeamName());
        assertThat(response.trackName()).isEqualTo(track.getTrackName());
        assertThat(response.submissionTypeName()).isEqualTo(item.getName());
        assertThat(response.status()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(response.commentCount()).isZero();
        assertThat(response.files()).hasSize(1);
        assertThat(response.files().get(0).fileName()).isEqualTo("발표자료.pdf");
        assertThat(response.files().get(0).fileSize()).isEqualTo(1048576L);
    }

    @Test
    @DisplayName("[성공] 마감 이후에 최초 제출된 제출물의 상태는 LATE이다.")
    void 마감_이후_제출물의_상태는_LATE이다() {
        joinTeam(member, team);
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(
                        contest, LocalDateTime.now().minusDays(2), true));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                        team.getId(), item, LocalDateTime.now().minusDays(1)));

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.status()).isEqualTo(SubmissionStatus.LATE);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물을 조회하면 예외가 발생한다.")
    void 존재하지_않는_제출물_조회_예외() {
        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionDetail(contest.getId(), 99999L, member))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[실패] 해당 팀 소속이 아닌 학생은 제출물 상세를 조회할 수 없다.")
    void 비소속_학생_상세_조회_예외() {
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), item));

        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }
}
