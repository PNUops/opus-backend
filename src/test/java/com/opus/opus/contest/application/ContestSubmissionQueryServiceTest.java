package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionFeedbackFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionDetailResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionTimelineResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionStatus;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
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
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.modules.team.exception.TeamMemberExceptionType;
import com.opus.opus.team.TeamFixture;
import java.time.LocalDateTime;
import java.util.List;
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
    @Autowired
    private ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;

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

    @Test
    @DisplayName("[성공] 제출 현황 요약을 조회하면 전체 항목 수와 제출 완료 수를 반환한다.")
    void 제출_현황_요약_조회() {
        joinTeam(member, team);
        contestSubmissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmissionItem item2 = contestSubmissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmissionItem item3 = contestSubmissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item2));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item3));

        final ContestSubmissionSummaryResponse response = contestSubmissionQueryService.getSubmissionSummary(
                contest.getId(), team.getId(), member);

        assertThat(response.totalItemCount()).isEqualTo(3);
        assertThat(response.submittedCount()).isEqualTo(2);
        assertThat(response.totalFeedbackCount()).isZero();
    }

    @Test
    @DisplayName("[성공] 제출물에 달린 피드백 총 수를 반환한다.")
    void 피드백_카운트_포함_요약_조회() {
        joinTeam(member, team);
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), item));
        final Member feedbackMember1 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        final Member feedbackMember2 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(2));
        contestSubmissionFeedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submission, feedbackMember1.getId()));
        contestSubmissionFeedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submission, feedbackMember2.getId()));

        final ContestSubmissionSummaryResponse response = contestSubmissionQueryService.getSubmissionSummary(
                contest.getId(), team.getId(), member);

        assertThat(response.totalFeedbackCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 제출 항목이 없는 대회는 모든 카운트가 0이다.")
    void 제출_항목_없는_대회_요약_조회() {
        joinTeam(member, team);

        final ContestSubmissionSummaryResponse response = contestSubmissionQueryService.getSubmissionSummary(
                contest.getId(), team.getId(), member);

        assertThat(response.totalItemCount()).isZero();
        assertThat(response.submittedCount()).isZero();
        assertThat(response.totalFeedbackCount()).isZero();
    }

    @Test
    @DisplayName("[실패] 비소속 학생은 제출 현황 요약을 조회할 수 없다.")
    void 비소속_학생_요약_조회_예외() {
        assertThatThrownBy(
                () -> contestSubmissionQueryService.getSubmissionSummary(contest.getId(), team.getId(), member))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 대회에 속하지 않은 팀으로 요약을 조회하면 예외가 발생한다.")
    void 대회_미소속_팀_요약_조회_예외() {
        final Contest otherContest = contestRepository.save(ContestFixture.createContest());

        assertThatThrownBy(
                () -> contestSubmissionQueryService.getSubmissionSummary(otherContest.getId(), team.getId(), member))
                .isInstanceOf(TeamException.class);
    }

    @Test
    @DisplayName("[성공] 제출 타임라인을 조회하면 제출 시각 오름차순으로 반환한다.")
    void 제출_타임라인_시간순_조회() {
        joinTeam(member, team);
        final ContestSubmissionItem item1 = contestSubmissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmissionItem item2 = contestSubmissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                team.getId(), item1, LocalDateTime.now().minusDays(2)));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                team.getId(), item2, LocalDateTime.now().minusDays(1)));

        final List<ContestSubmissionTimelineResponse> responses = contestSubmissionQueryService.getSubmissionTimeline(
                contest.getId(), team.getId(), member);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).submissionItemName()).isEqualTo(item1.getName());
        assertThat(responses.get(1).submissionItemName()).isEqualTo(item2.getName());
    }

    @Test
    @DisplayName("[성공] 제출물이 없으면 빈 타임라인을 반환한다.")
    void 제출_없는_팀_타임라인_빈_리스트() {
        joinTeam(member, team);

        final List<ContestSubmissionTimelineResponse> responses = contestSubmissionQueryService.getSubmissionTimeline(
                contest.getId(), team.getId(), member);

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("[성공] 마감 이후에 제출된 항목의 타임라인 상태는 LATE이다.")
    void 지각_제출_타임라인_상태_LATE() {
        joinTeam(member, team);
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(
                        contest, LocalDateTime.now().minusDays(2), true));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                team.getId(), item, LocalDateTime.now().minusDays(1)));

        final List<ContestSubmissionTimelineResponse> responses = contestSubmissionQueryService.getSubmissionTimeline(
                contest.getId(), team.getId(), member);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).status()).isEqualTo(SubmissionStatus.LATE);
    }

    @Test
    @DisplayName("[실패] 비소속 학생은 제출 타임라인을 조회할 수 없다.")
    void 비소속_학생_타임라인_조회_예외() {
        assertThatThrownBy(
                () -> contestSubmissionQueryService.getSubmissionTimeline(contest.getId(), team.getId(), member))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }
}
