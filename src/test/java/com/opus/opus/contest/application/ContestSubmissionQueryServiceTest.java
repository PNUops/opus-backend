package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_ALLOWED_TO_VIEW_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;
import static java.time.LocalDateTime.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestMemberFixture;
import com.opus.opus.contest.ContestSubmissionFeedbackFixture;
import com.opus.opus.contest.ContestSubmissionFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionDetailResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionStatusResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionTimelineResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionItemResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.UpcomingSubmissionResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import com.opus.opus.modules.contest.application.SubmissionStatus;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
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
import com.opus.opus.modules.member.domain.MemberRoleType;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamExceptionType;
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
    @Autowired
    private ContestMemberRepository contestMemberRepository;

    private Contest contest;
    private Team team;
    private Member member;
    private ContestTrack track;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
    }

    private void prepareTeamAndMember() {
        team = teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        member = memberRepository.save(MemberFixture.createMember());
    }

    private void joinTeam(final Member loginMember, final Team joinTeam) {
        teamMemberRepository.save(TeamMember.builder()
                .memberId(loginMember.getId())
                .team(joinTeam)
                .roles(Set.of(TeamMemberRoleType.ROLE_팀장))
                .build());
    }

    private Team saveTeam(final String teamName) {
        return teamRepository.save(TeamFixture.createTeamWithContestIdAndTeamName(contest.getId(), teamName));
    }

    private ContestSubmission saveSubmissionWithVisibility(final SubmissionVisibility visibility) {
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithVisibility(contest, visibility));
        return contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item));
    }

    @Test
    @DisplayName("[성공] 팀원이 제출물 상세를 조회하면 기본 정보와 파일 목록을 반환한다.")
    void 제출물_상세를_조회한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
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
        assertThat(response.submissionItemName()).isEqualTo(item.getName());
        assertThat(response.status()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(response.feedbackCount()).isZero();
        assertThat(response.files()).hasSize(1);
        assertThat(response.files().get(0).fileName()).isEqualTo("발표자료.pdf");
        assertThat(response.files().get(0).fileSize()).isEqualTo(1048576L);
    }

    @Test
    @DisplayName("[성공] 제출물 상세 조회 시 제출물에 달린 피드백 개수를 반환한다.")
    void 제출물_상세의_피드백_개수를_반환한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest, track));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), item));
        final Member feedbackMember1 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(1));
        final Member feedbackMember2 = memberRepository.save(MemberFixture.createMemberWithUniqueNum(2));
        contestSubmissionFeedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submission, feedbackMember1.getId()));
        contestSubmissionFeedbackRepository.save(
                ContestSubmissionFeedbackFixture.createFeedback(submission, feedbackMember2.getId()));

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.feedbackCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 마감 이후에 최초 제출된 제출물의 상태는 LATE이다.")
    void 마감_이후_제출물의_상태는_LATE이다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(2), true));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(team.getId(), item, now().minusDays(1)));

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.status()).isEqualTo(SubmissionStatus.LATE);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출물을 조회하면 예외가 발생한다.")
    void 존재하지_않는_제출물_조회_예외() {
        prepareTeamAndMember();
        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionDetail(contest.getId(), 99999L, member))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[성공] PUBLIC 제출물은 팀 비소속 회원도 조회할 수 있다.")
    void PUBLIC_제출물은_비소속_회원도_조회한다() {
        prepareTeamAndMember();
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.PUBLIC);

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[성공] MEMBER 제출물은 로그인한 회원이면 조회할 수 있다.")
    void MEMBER_제출물은_로그인_회원이_조회한다() {
        prepareTeamAndMember();
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.MEMBER);

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[성공] TEAM 제출물은 제출 팀의 팀원이 조회할 수 있다.")
    void TEAM_제출물은_팀원이_조회한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.TEAM);

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[성공] TEAM 제출물은 관리자가 조회할 수 있다.")
    void TEAM_제출물은_관리자가_조회한다() {
        prepareTeamAndMember();
        final Member admin = memberRepository.save(
                MemberFixture.createMemberWithRole("관리자", 1, MemberRoleType.ROLE_관리자));
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.TEAM);

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), admin);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[실패] TEAM 제출물은 제출 팀 소속이 아닌 회원이 조회할 수 없다.")
    void TEAM_제출물_비소속_회원_조회_예외() {
        prepareTeamAndMember();
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.TEAM);

        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_TO_VIEW_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[성공] STAFF 제출물은 제출 팀의 팀원이 조회할 수 있다.")
    void STAFF_제출물은_팀원이_조회한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.STAFF);

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[성공] STAFF 제출물은 해당 팀에 지정된 교수가 조회할 수 있다.")
    void STAFF_제출물은_지정된_교수가_조회한다() {
        prepareTeamAndMember();
        final Member professor = memberRepository.save(
                MemberFixture.createMemberWithRole("교수", 1, MemberRoleType.ROLE_교수));
        contestMemberRepository.save(
                ContestMemberFixture.createContestMember(contest, professor.getId(), List.of(team.getId())));
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.STAFF);

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), professor);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[성공] STAFF 제출물은 해당 팀에 지정된 외부멘토가 조회할 수 있다.")
    void STAFF_제출물은_지정된_외부멘토가_조회한다() {
        prepareTeamAndMember();
        final Member mentor = memberRepository.save(
                MemberFixture.createMemberWithRole("멘토", 1, MemberRoleType.ROLE_외부멘토));
        contestMemberRepository.save(
                ContestMemberFixture.createContestMember(contest, mentor.getId(), List.of(team.getId())));
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.STAFF);

        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), mentor);

        assertThat(response.submissionId()).isEqualTo(submission.getId());
    }

    @Test
    @DisplayName("[실패] STAFF 제출물은 해당 팀에 지정되지 않은 교수가 조회할 수 없다.")
    void STAFF_제출물_미지정_교수_조회_예외() {
        prepareTeamAndMember();
        final Member professor = memberRepository.save(
                MemberFixture.createMemberWithRole("교수", 1, MemberRoleType.ROLE_교수));
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.STAFF);

        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), professor))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_TO_VIEW_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[실패] STAFF 제출물은 제출 팀 소속이 아닌 일반 회원이 조회할 수 없다.")
    void STAFF_제출물_비소속_회원_조회_예외() {
        prepareTeamAndMember();
        final ContestSubmission submission = saveSubmissionWithVisibility(SubmissionVisibility.STAFF);

        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionDetail(
                contest.getId(), submission.getId(), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_ALLOWED_TO_VIEW_SUBMISSION.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출 항목 기준으로 전체 팀의 제출 현황을 조회한다.")
    void 제출_항목_기준_전체_팀_현황을_조회한다() {
        final Team submitted = saveTeam("오퍼스");
        final Team notSubmitted = saveTeam("팀B");
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(submitted.getId(), item));

        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contest.getId(), item.getId(), null, null, null);

        assertThat(responses).hasSize(2);
        final ContestSubmissionStatusResponse submittedRow = findByTeam(responses, submitted.getId());
        assertThat(submittedRow.submissionItemName()).isEqualTo(item.getName());
        assertThat(submittedRow.status()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(submittedRow.firstSubmittedAt()).isNotNull();

        final ContestSubmissionStatusResponse notSubmittedRow = findByTeam(responses, notSubmitted.getId());
        assertThat(notSubmittedRow.status()).isEqualTo(SubmissionStatus.NOT_SUBMITTED);
        assertThat(notSubmittedRow.submissionId()).isNull();
        assertThat(notSubmittedRow.firstSubmittedAt()).isNull();
    }

    @Test
    @DisplayName("[성공] 마감 이후 미제출 팀의 상태는 NOT_SUBMITTED_AFTER_DEADLINE이다.")
    void 마감_이후_미제출_팀의_상태는_AFTER_DEADLINE이다() {
        saveTeam("팀A");
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(1), false));

        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contest.getId(), item.getId(), null, null, null);

        assertThat(responses).singleElement()
                .extracting(ContestSubmissionStatusResponse::status)
                .isEqualTo(SubmissionStatus.NOT_SUBMITTED_AFTER_DEADLINE);
    }

    @Test
    @DisplayName("[성공] 마감 이후 최초 제출된 팀의 상태는 LATE이다.")
    void 마감_이후_제출된_팀의_상태는_LATE이다() {
        final Team lateTeam = saveTeam("팀A");
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(2), true));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                lateTeam.getId(), item, now().minusDays(1)));

        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contest.getId(), item.getId(), null, null, null);

        assertThat(responses).singleElement()
                .extracting(ContestSubmissionStatusResponse::status)
                .isEqualTo(SubmissionStatus.LATE);
    }

    @Test
    @DisplayName("[성공] 제출 상태로 필터링하면 해당 상태의 팀만 조회된다.")
    void 제출_상태로_필터링한다() {
        final Team submitted = saveTeam("제출팀");
        saveTeam("미제출팀");
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(submitted.getId(), item));

        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contest.getId(), item.getId(), SubmissionStatus.SUBMITTED, null, null);

        assertThat(responses).singleElement()
                .extracting(ContestSubmissionStatusResponse::teamId)
                .isEqualTo(submitted.getId());
    }

    @Test
    @DisplayName("[성공] 분과로 필터링하면 해당 분과 팀만 조회된다.")
    void 분과로_필터링한다() {
        final ContestTrack otherTrack = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final Team teamInTrack = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), otherTrack.getId()));
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));

        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contest.getId(), item.getId(), null, track.getId(), null);

        assertThat(responses).singleElement()
                .extracting(ContestSubmissionStatusResponse::teamId)
                .isEqualTo(teamInTrack.getId());
    }

    @Test
    @DisplayName("[성공] 팀 이름 키워드로 검색한다.")
    void 팀_이름_키워드로_검색한다() {
        final Team target = saveTeam("오퍼스");
        saveTeam("다른팀");
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));

        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contest.getId(), item.getId(), null, null, "오퍼");

        assertThat(responses).singleElement()
                .extracting(ContestSubmissionStatusResponse::teamId)
                .isEqualTo(target.getId());
    }

    @Test
    @DisplayName("[성공] 제출 항목를 지정하지 않으면 팀×제출 항목 모든 조합을 조회한다.")
    void 항목_미지정시_팀과_항목의_모든_조합을_조회한다() {
        saveTeam("팀A");
        saveTeam("팀B");
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));

        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contest.getId(), null, null, null, null);

        assertThat(responses).hasSize(4);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회를 조회하면 예외가 발생한다.")
    void 존재하지_않는_대회_조회_예외() {
        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionStatuses(
                99999L, null, null, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출 항목 기준으로 제출 현황 통계를 집계한다.")
    void 제출_현황_통계를_집계한다() {
        final Team submitted = saveTeam("제출팀");
        final Team late = saveTeam("지각팀");
        saveTeam("미제출팀");
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(3), true));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                submitted.getId(), item, now().minusDays(4)));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                late.getId(), item, now().minusDays(1)));

        final ContestSubmissionSummaryResponse response = contestSubmissionQueryService.getSubmissionSummary(
                contest.getId(), item.getId(), null);

        assertThat(response.totalTeams()).isEqualTo(3);
        assertThat(response.submittedCount()).isEqualTo(1);
        assertThat(response.notSubmittedCount()).isEqualTo(1);
        assertThat(response.lateCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 미제출 통계는 마감 전 미제출과 마감 후 미제출을 합산한다.")
    void 미제출_통계는_마감_전후_미제출을_합산한다() {
        saveTeam("마감전팀");
        saveTeam("마감후팀");
        contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(1), false));
        contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(1), false));

        final ContestSubmissionSummaryResponse response = contestSubmissionQueryService.getSubmissionSummary(
                contest.getId(), null, null);

        assertThat(response.totalTeams()).isEqualTo(4);
        assertThat(response.notSubmittedCount()).isEqualTo(4);
        assertThat(response.submittedCount()).isZero();
        assertThat(response.lateCount()).isZero();
    }

    @Test
    @DisplayName("[성공] 분과로 필터링하면 해당 분과 팀만 통계에 집계된다.")
    void 분과로_필터링해_통계를_집계한다() {
        final ContestTrack otherTrack = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), track.getId()));
        teamRepository.save(TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), otherTrack.getId()));
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));

        final ContestSubmissionSummaryResponse response = contestSubmissionQueryService.getSubmissionSummary(
                contest.getId(), null, track.getId());

        assertThat(response.totalTeams()).isEqualTo(1);
        assertThat(response.notSubmittedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 제출 현황 통계를 조회하면 예외가 발생한다.")
    void 존재하지_않는_대회_통계_조회_예외() {
        assertThatThrownBy(() -> contestSubmissionQueryService.getSubmissionSummary(99999L, null, null))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 팀원이 팀의 제출물 항목별 현황을 제출/미제출 구분과 함께 조회한다.")
    void 팀_제출물_현황을_조회한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem submittedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));
        final ContestSubmissionItem notSubmittedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));
        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmissionFixture.createSubmission(team.getId(), submittedItem));
        final File file = File.create("발표자료.pdf", "files/2026-06-24/a.pdf", "application/pdf", 1048576L);
        fileDocumentRepository.save(FileDocument.builder()
                .file(file).submissionId(submission.getId()).fileOrder(1).build());

        final List<TeamSubmissionItemResponse> responses = contestSubmissionQueryService.getTeamSubmissionStatuses(
                contest.getId(), team.getId(), null, member);

        assertThat(responses).hasSize(2);
        final TeamSubmissionItemResponse submittedRow = findByItem(responses, submittedItem.getId());
        assertThat(submittedRow.submissionId()).isEqualTo(submission.getId());
        assertThat(submittedRow.submissionItemName()).isEqualTo(submittedItem.getName());
        assertThat(submittedRow.status()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(submittedRow.files()).hasSize(1);
        assertThat(submittedRow.files().get(0).fileName()).isEqualTo("발표자료.pdf");
        assertThat(submittedRow.files().get(0).fileSize()).isEqualTo(1048576L);

        final TeamSubmissionItemResponse notSubmittedRow = findByItem(responses, notSubmittedItem.getId());
        assertThat(notSubmittedRow.submissionId()).isNull();
        assertThat(notSubmittedRow.status()).isEqualTo(SubmissionStatus.NOT_SUBMITTED);
        assertThat(notSubmittedRow.files()).isEmpty();
    }

    @Test
    @DisplayName("[성공] 제출 상태로 필터링하면 해당 상태의 제출물 항목만 조회된다.")
    void 팀_제출물_현황을_상태로_필터링한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem submittedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest));
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), submittedItem));

        final List<TeamSubmissionItemResponse> responses = contestSubmissionQueryService.getTeamSubmissionStatuses(
                contest.getId(), team.getId(), SubmissionStatus.SUBMITTED, member);

        assertThat(responses).singleElement()
                .extracting(TeamSubmissionItemResponse::submissionItemId)
                .isEqualTo(submittedItem.getId());
    }

    @Test
    @DisplayName("[성공] 마감 이후 미제출 항목의 상태는 NOT_SUBMITTED_AFTER_DEADLINE이다.")
    void 팀_마감_이후_미제출_항목의_상태는_AFTER_DEADLINE이다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(1), false));

        final List<TeamSubmissionItemResponse> responses = contestSubmissionQueryService.getTeamSubmissionStatuses(
                contest.getId(), team.getId(), null, member);

        assertThat(responses).singleElement()
                .extracting(TeamSubmissionItemResponse::status)
                .isEqualTo(SubmissionStatus.NOT_SUBMITTED_AFTER_DEADLINE);
    }

    @Test
    @DisplayName("[성공] 마감 이후 최초 제출된 항목의 상태는 LATE이다.")
    void 팀_마감_이후_제출된_항목의_상태는_LATE이다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem item = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(2), true));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmissionWithFirstSubmittedAt(
                team.getId(), item, now().minusDays(1)));

        final List<TeamSubmissionItemResponse> responses = contestSubmissionQueryService.getTeamSubmissionStatuses(
                contest.getId(), team.getId(), null, member);

        assertThat(responses).singleElement()
                .extracting(TeamSubmissionItemResponse::status)
                .isEqualTo(SubmissionStatus.LATE);
    }

    @Test
    @DisplayName("[성공] 관리자는 팀 소속이 아니어도 팀의 제출물 현황을 조회할 수 있다.")
    void 관리자는_비소속_팀의_현황을_조회한다() {
        prepareTeamAndMember();
        final Member admin = memberRepository.save(
                MemberFixture.createMemberWithRole("관리자", 1, MemberRoleType.ROLE_관리자));
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));

        final List<TeamSubmissionItemResponse> responses = contestSubmissionQueryService.getTeamSubmissionStatuses(
                contest.getId(), team.getId(), null, admin);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("[실패] 해당 팀 소속이 아닌 학생은 팀의 제출물 현황을 조회할 수 없다.")
    void 비소속_학생_팀_현황_조회_예외() {
        prepareTeamAndMember();
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));

        assertThatThrownBy(() -> contestSubmissionQueryService.getTeamSubmissionStatuses(
                contest.getId(), team.getId(), null, member))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 팀 제출물 현황을 조회하면 예외가 발생한다.")
    void 존재하지_않는_대회_팀_현황_조회_예외() {
        prepareTeamAndMember();
        assertThatThrownBy(() -> contestSubmissionQueryService.getTeamSubmissionStatuses(
                99999L, team.getId(), null, member))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀의 제출물 현황을 조회하면 예외가 발생한다.")
    void 존재하지_않는_팀_현황_조회_예외() {
        prepareTeamAndMember();
        assertThatThrownBy(() -> contestSubmissionQueryService.getTeamSubmissionStatuses(
                contest.getId(), 99999L, null, member))
                .isInstanceOf(TeamException.class)
                .hasMessage(TeamExceptionType.NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 마감 예정 제출 항목을 마감일 오름차순으로 조회한다.")
    void 마감_예정_제출_항목을_마감일_오름차순으로_조회한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem laterItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(5), false));
        final ContestSubmissionItem earlierItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(1), false));

        final List<UpcomingSubmissionResponse> responses = contestSubmissionQueryService.getUpcomingTeamSubmissions(
                contest.getId(), team.getId(), member);

        assertThat(responses).extracting(UpcomingSubmissionResponse::submissionItemId)
                .containsExactly(earlierItem.getId(), laterItem.getId());
    }

    @Test
    @DisplayName("[성공] 마감일이 지난 항목은 반환하지 않는다.")
    void 마감일이_지난_항목은_반환하지_않는다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem upcomingItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(1), false));
        contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().minusDays(1), false));

        final List<UpcomingSubmissionResponse> responses = contestSubmissionQueryService.getUpcomingTeamSubmissions(
                contest.getId(), team.getId(), member);

        assertThat(responses).singleElement()
                .extracting(UpcomingSubmissionResponse::submissionItemId)
                .isEqualTo(upcomingItem.getId());
    }

    @Test
    @DisplayName("[성공] 제출 여부에 따라 상태와 최종 수정일시를 반환한다.")
    void 제출_여부에_따라_상태와_최종_수정일시를_반환한다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestSubmissionItem submittedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(1), false));
        final ContestSubmissionItem notSubmittedItem = contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(2), false));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), submittedItem));

        final List<UpcomingSubmissionResponse> responses = contestSubmissionQueryService.getUpcomingTeamSubmissions(
                contest.getId(), team.getId(), member);

        final UpcomingSubmissionResponse submittedRow = findUpcomingByItem(responses, submittedItem.getId());
        assertThat(submittedRow.status()).isEqualTo(SubmissionStatus.SUBMITTED);
        assertThat(submittedRow.lastModifiedAt()).isNotNull();

        final UpcomingSubmissionResponse notSubmittedRow = findUpcomingByItem(responses, notSubmittedItem.getId());
        assertThat(notSubmittedRow.status()).isEqualTo(SubmissionStatus.NOT_SUBMITTED);
        assertThat(notSubmittedRow.lastModifiedAt()).isNull();
    }

    @Test
    @DisplayName("[성공] 관리자는 팀 소속이 아니어도 팀의 마감 예정 제출 항목을 조회할 수 있다.")
    void 관리자는_비소속_팀의_마감_예정_항목을_조회한다() {
        prepareTeamAndMember();
        final Member admin = memberRepository.save(
                MemberFixture.createMemberWithRole("관리자", 1, MemberRoleType.ROLE_관리자));
        contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(1), false));

        final List<UpcomingSubmissionResponse> responses = contestSubmissionQueryService.getUpcomingTeamSubmissions(
                contest.getId(), team.getId(), admin);

        assertThat(responses).hasSize(1);
    }

    @Test
    @DisplayName("[실패] 해당 팀 소속이 아닌 학생은 팀의 마감 예정 제출 항목을 조회할 수 없다.")
    void 비소속_학생_마감_예정_조회_예외() {
        prepareTeamAndMember();
        contestSubmissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItemWithDeadline(contest, now().plusDays(1), false));

        assertThatThrownBy(() -> contestSubmissionQueryService.getUpcomingTeamSubmissions(
                contest.getId(), team.getId(), member))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 마감 예정 제출 항목을 조회하면 예외가 발생한다.")
    void 존재하지_않는_대회_마감_예정_조회_예외() {
        prepareTeamAndMember();
        assertThatThrownBy(() -> contestSubmissionQueryService.getUpcomingTeamSubmissions(
                99999L, team.getId(), member))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 팀의 마감 예정 제출 항목을 조회하면 예외가 발생한다.")
    void 존재하지_않는_팀_마감_예정_조회_예외() {
        prepareTeamAndMember();
        assertThatThrownBy(() -> contestSubmissionQueryService.getUpcomingTeamSubmissions(
                contest.getId(), 99999L, member))
                .isInstanceOf(TeamException.class)
                .hasMessage(TeamExceptionType.NOT_FOUND_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출 현황 요약을 조회하면 전체 항목 수와 제출 완료 수를 반환한다.")
    void 제출_현황_요약_조회() {
        prepareTeamAndMember();
        joinTeam(member, team);
        contestSubmissionItemRepository.save(ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmissionItem item2 = contestSubmissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        final ContestSubmissionItem item3 = contestSubmissionItemRepository.save(
                ContestSubmissionFixture.createSubmissionItem(contest));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item2));
        contestSubmissionRepository.save(ContestSubmissionFixture.createSubmission(team.getId(), item3));

        final TeamSubmissionSummaryResponse response = contestSubmissionQueryService.getTeamSubmissionSummary(
                contest.getId(), team.getId(), member);

        assertThat(response.totalItemCount()).isEqualTo(3);
        assertThat(response.submittedCount()).isEqualTo(2);
        assertThat(response.totalFeedbackCount()).isZero();
    }

    @Test
    @DisplayName("[성공] 제출 현황 요약의 전체 항목 수는 팀 분과에 해당하는 항목만 센다.")
    void 요약_전체_항목_수는_팀_분과_항목만_센다() {
        prepareTeamAndMember();
        joinTeam(member, team);
        final ContestTrack otherTrack = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest, track));
        contestSubmissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest, otherTrack));

        final TeamSubmissionSummaryResponse response = contestSubmissionQueryService.getTeamSubmissionSummary(
                contest.getId(), team.getId(), member);

        assertThat(response.totalItemCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 제출물에 달린 피드백 총 수를 반환한다.")
    void 피드백_카운트_포함_요약_조회() {
        prepareTeamAndMember();
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

        final TeamSubmissionSummaryResponse response = contestSubmissionQueryService.getTeamSubmissionSummary(
                contest.getId(), team.getId(), member);

        assertThat(response.totalFeedbackCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 제출 항목이 없는 대회는 모든 카운트가 0이다.")
    void 제출_항목_없는_대회_요약_조회() {
        prepareTeamAndMember();
        joinTeam(member, team);

        final TeamSubmissionSummaryResponse response = contestSubmissionQueryService.getTeamSubmissionSummary(
                contest.getId(), team.getId(), member);

        assertThat(response.totalItemCount()).isZero();
        assertThat(response.submittedCount()).isZero();
        assertThat(response.totalFeedbackCount()).isZero();
    }

    @Test
    @DisplayName("[실패] 비소속 학생은 제출 현황 요약을 조회할 수 없다.")
    void 비소속_학생_요약_조회_예외() {
        prepareTeamAndMember();
        assertThatThrownBy(
                () -> contestSubmissionQueryService.getTeamSubmissionSummary(contest.getId(), team.getId(), member))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 대회에 속하지 않은 팀으로 요약을 조회하면 예외가 발생한다.")
    void 대회_미소속_팀_요약_조회_예외() {
        prepareTeamAndMember();
        final Contest otherContest = contestRepository.save(ContestFixture.createContest());

        assertThatThrownBy(
                () -> contestSubmissionQueryService.getTeamSubmissionSummary(otherContest.getId(), team.getId(), member))
                .isInstanceOf(TeamException.class);
    }

    @Test
    @DisplayName("[성공] 제출 타임라인을 조회하면 제출 시각 오름차순으로 반환한다.")
    void 제출_타임라인_시간순_조회() {
        prepareTeamAndMember();
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
        prepareTeamAndMember();
        joinTeam(member, team);

        final List<ContestSubmissionTimelineResponse> responses = contestSubmissionQueryService.getSubmissionTimeline(
                contest.getId(), team.getId(), member);

        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("[성공] 마감 이후에 제출된 항목의 타임라인 상태는 LATE이다.")
    void 지각_제출_타임라인_상태_LATE() {
        prepareTeamAndMember();
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
        prepareTeamAndMember();
        assertThatThrownBy(
                () -> contestSubmissionQueryService.getSubmissionTimeline(contest.getId(), team.getId(), member))
                .isInstanceOf(TeamMemberException.class)
                .hasMessage(TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM.errorMessage());
    }

    private UpcomingSubmissionResponse findUpcomingByItem(final List<UpcomingSubmissionResponse> responses,
                                                          final Long submissionItemId) {
        return responses.stream()
                .filter(response -> response.submissionItemId().equals(submissionItemId))
                .findFirst()
                .orElseThrow();
    }

    private ContestSubmissionStatusResponse findByTeam(final List<ContestSubmissionStatusResponse> responses,
                                                       final Long teamId) {
        return responses.stream()
                .filter(response -> response.teamId().equals(teamId))
                .findFirst()
                .orElseThrow();
    }

    private TeamSubmissionItemResponse findByItem(final List<TeamSubmissionItemResponse> responses,
                                                  final Long submissionItemId) {
        return responses.stream()
                .filter(response -> response.submissionItemId().equals(submissionItemId))
                .findFirst()
                .orElseThrow();
    }
}
