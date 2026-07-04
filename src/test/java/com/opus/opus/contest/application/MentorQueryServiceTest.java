package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestMemberFixture.createContestMember;
import static com.opus.opus.contest.ContestSubmissionFeedbackFixture.createFeedback;
import static com.opus.opus.contest.ContestSubmissionFixture.createSubmission;
import static com.opus.opus.member.MemberFixture.createMemberWithRole;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_ASSIGNED_TEAM;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_FOUND_CONTEST_MEMBER;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.opus.opus.contest.ContestCategoryFixture;
import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.MentorQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFileResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorContestResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse.FeedbackStatus;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
import com.opus.opus.modules.contest.domain.dao.ContestCategoryRepository;
import com.opus.opus.modules.contest.domain.dao.ContestMemberRepository;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestMemberException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MentorQueryServiceTest extends IntegrationTest {

    @Autowired
    private MentorQueryService mentorQueryService;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestCategoryRepository contestCategoryRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private ContestMemberRepository contestMemberRepository;
    @Autowired
    private ContestSubmissionItemRepository contestSubmissionItemRepository;
    @Autowired
    private ContestSubmissionRepository contestSubmissionRepository;
    @Autowired
    private ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;
    @Autowired
    private FileDocumentRepository fileDocumentRepository;

    private ContestCategory category;
    private Contest contest;
    private ContestTrack track;
    private Member mentor;
    private Team developTeam;
    private Team planningTeam;
    private ContestSubmission reviewedSubmission;
    private ContestSubmission pendingSubmission;

    @BeforeEach
    void setUp() {
        category = contestCategoryRepository.save(ContestCategoryFixture.createContestCategory());
        contest = contestRepository.save(ContestFixture.createContestWithCategoryId(category.getId()));
        track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        mentor = memberRepository.save(createMemberWithRole("김멘토", 1, ROLE_외부멘토));
        developTeam = teamRepository.save(buildTeam("개발 1팀"));
        planningTeam = teamRepository.save(buildTeam("운영 기획팀"));

        contestMemberRepository.save(
                createContestMember(contest, mentor.getId(), List.of(developTeam.getId(), planningTeam.getId())));

        final ContestSubmissionItem midItem = saveItem("중간발표 자료", SubmissionVisibility.PUBLIC);
        final ContestSubmissionItem finalItem = saveItem("최종발표 자료", SubmissionVisibility.PUBLIC);
        final ContestSubmissionItem staffItem = saveItem("심사용 자료", SubmissionVisibility.STAFF);
        final ContestSubmissionItem memberItem = saveItem("회원 공개 자료", SubmissionVisibility.MEMBER);
        final ContestSubmissionItem teamItem = saveItem("팀 내부 자료", SubmissionVisibility.TEAM);

        reviewedSubmission = contestSubmissionRepository.save(createSubmission(developTeam.getId(), midItem));
        pendingSubmission = contestSubmissionRepository.save(createSubmission(developTeam.getId(), finalItem));
        contestSubmissionRepository.save(createSubmission(developTeam.getId(), staffItem));
        contestSubmissionRepository.save(createSubmission(developTeam.getId(), memberItem));
        contestSubmissionRepository.save(createSubmission(developTeam.getId(), teamItem));

        contestSubmissionFeedbackRepository.save(createFeedback(reviewedSubmission, mentor.getId()));
        saveFile(reviewedSubmission.getId(), "중간발표.pdf", 13002342L);
        saveFile(pendingSubmission.getId(), "최종발표.pdf", 20480L);
    }

    @Test
    @DisplayName("[성공] 담당 대회 목록을 조회한다.")
    void 담당_대회_목록을_조회한다() {
        final List<MentorContestResponse> contests = mentorQueryService.getMentorContests(mentor);

        assertThat(contests).hasSize(1);

        final MentorContestResponse response = contests.get(0);
        assertThat(response.contestId()).isEqualTo(contest.getId());
        assertThat(response.contestName()).isEqualTo(contest.getContestName());
        assertThat(response.categoryName()).isEqualTo(category.getCategoryName());
        assertThat(response.assignedTrackNames()).containsExactly(track.getTrackName());
        assertThat(response.totalPendingFeedbackCount()).isEqualTo(3);
        assertThat(response.totalAssignedTeamCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("[성공] 배정되지 않은 멘토는 빈 대회 목록을 반환한다.")
    void 배정되지_않은_멘토는_빈_대회_목록을_반환한다() {
        final Member unassigned = memberRepository.save(createMemberWithRole("이멘토", 2, ROLE_외부멘토));

        assertThat(mentorQueryService.getMentorContests(unassigned)).isEmpty();
    }

    @Test
    @DisplayName("[성공] 여러 대회에 배정된 멘토는 대회별로 집계된 목록을 조회한다.")
    void 여러_대회에_배정된_멘토는_대회별로_집계된_목록을_조회한다() {
        final Contest contest2 = contestRepository.save(ContestFixture.createContestWithCategoryId(category.getId()));
        final ContestTrack track2 = contestTrackRepository.save(ContestTrackFixture.createTrack(contest2));
        final Team team2 = teamRepository.save(Team.builder()
                .teamName("2대회팀")
                .projectName("두번째 프로젝트")
                .contestId(contest2.getId())
                .trackId(track2.getId())
                .itemOrder(1)
                .build());
        contestMemberRepository.save(createContestMember(contest2, mentor.getId(), List.of(team2.getId())));

        final List<MentorContestResponse> contests = mentorQueryService.getMentorContests(mentor);

        assertThat(contests).extracting(MentorContestResponse::contestId)
                .containsExactlyInAnyOrder(contest.getId(), contest2.getId());

        final MentorContestResponse response2 = findContest(contests, contest2.getId());
        assertThat(response2.assignedTrackNames()).containsExactly(track2.getTrackName());
        assertThat(response2.totalPendingFeedbackCount()).isEqualTo(0);
        assertThat(response2.totalAssignedTeamCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("[성공] 선택한 대회의 담당 팀 목록을 조회한다.")
    void 선택한_대회의_담당_팀_목록을_조회한다() {
        final List<MentorProjectResponse> teams =
                mentorQueryService.getMentorContestTeams(contest.getId(), mentor);

        assertThat(teams).hasSize(2);

        final MentorProjectResponse develop = findProject(teams, developTeam.getId());
        assertThat(develop.projectName()).isEqualTo("옵스 프로젝트");
        assertThat(develop.trackName()).isEqualTo(track.getTrackName());
        assertThat(develop.roleType()).isEqualTo(ROLE_외부멘토.name());
        assertThat(develop.pendingFeedbackCount()).isEqualTo(3);

        final MentorProjectResponse planning = findProject(teams, planningTeam.getId());
        assertThat(planning.pendingFeedbackCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 담당 팀 목록은 조회할 수 없다.")
    void 존재하지_않는_대회의_담당_팀_목록은_조회할_수_없다() {
        assertThatThrownBy(() -> mentorQueryService.getMentorContestTeams(-1L, mentor))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[실패] 배정되지 않은 대회의 담당 팀 목록은 조회할 수 없다.")
    void 배정되지_않은_대회의_담당_팀_목록은_조회할_수_없다() {
        final Member unassigned = memberRepository.save(createMemberWithRole("이멘토", 2, ROLE_외부멘토));

        assertThatThrownBy(() -> mentorQueryService.getMentorContestTeams(contest.getId(), unassigned))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(NOT_FOUND_CONTEST_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 담당 팀의 열람 가능한 제출물과 피드백 상태를 조회한다.")
    void 담당_팀의_열람_가능한_제출물과_피드백_상태를_조회한다() {
        final TeamSubmissionsResponse response =
                mentorQueryService.getTeamSubmissions(contest.getId(), developTeam.getId(), mentor);

        assertThat(response.teamId()).isEqualTo(developTeam.getId());
        assertThat(response.trackName()).isEqualTo(track.getTrackName());
        assertThat(response.pendingFeedbackCount()).isEqualTo(3);
        assertThat(response.submissions())
                .extracting(MentorSubmissionResponse::feedbackStatus)
                .containsExactly(FeedbackStatus.COMPLETED, FeedbackStatus.PENDING,
                        FeedbackStatus.PENDING, FeedbackStatus.PENDING);

        final MentorSubmissionResponse reviewed = response.submissions().get(0);
        assertThat(reviewed.submissionItemName()).isEqualTo("중간발표 자료");
        assertThat(reviewed.files())
                .extracting(ContestSubmissionFileResponse::fileName, ContestSubmissionFileResponse::fileSize)
                .containsExactly(tuple("중간발표.pdf", 13002342L));
    }

    @Test
    @DisplayName("[성공] 관계자 조회 목록은 PUBLIC·MEMBER·STAFF 제출물을 포함하고 TEAM 제출물은 제외한다.")
    void 관계자_조회_목록은_TEAM_제출물을_제외한다() {
        final TeamSubmissionsResponse response =
                mentorQueryService.getTeamSubmissions(contest.getId(), developTeam.getId(), mentor);

        assertThat(response.submissions())
                .extracting(MentorSubmissionResponse::submissionItemName)
                .containsExactly("중간발표 자료", "최종발표 자료", "심사용 자료", "회원 공개 자료")
                .doesNotContain("팀 내부 자료");
    }

    @Test
    @DisplayName("[실패] 본인 담당 팀이 아니면 조회할 수 없다.")
    void 본인_담당_팀이_아니면_조회할_수_없다() {
        final Member otherMentor = memberRepository.save(createMemberWithRole("박멘토", 3, ROLE_외부멘토));
        final Team otherTeam = teamRepository.save(buildTeam("보안 2팀"));
        contestMemberRepository.save(createContestMember(contest, otherMentor.getId(), List.of(otherTeam.getId())));

        assertThatThrownBy(() ->
                mentorQueryService.getTeamSubmissions(contest.getId(), developTeam.getId(), otherMentor))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(NOT_ASSIGNED_TEAM.errorMessage());
    }

    private MentorContestResponse findContest(final List<MentorContestResponse> contests, final Long contestId) {
        return contests.stream()
                .filter(response -> response.contestId().equals(contestId))
                .findFirst()
                .orElseThrow();
    }

    private MentorProjectResponse findProject(final List<MentorProjectResponse> projects, final Long teamId) {
        return projects.stream()
                .filter(project -> project.teamId().equals(teamId))
                .findFirst()
                .orElseThrow();
    }

    private Team buildTeam(final String teamName) {
        return Team.builder()
                .teamName(teamName)
                .projectName("옵스 프로젝트")
                .contestId(contest.getId())
                .trackId(track.getId())
                .itemOrder(1)
                .build();
    }

    private ContestSubmissionItem saveItem(final String name, final SubmissionVisibility visibility) {
        return contestSubmissionItemRepository.save(ContestSubmissionItem.builder()
                .name(name)
                .maxFileSizeMb(10)
                .maxFileCount(5)
                .startAt(LocalDateTime.now().minusDays(1))
                .endAt(LocalDateTime.now().plusDays(7))
                .allowLateSubmission(false)
                .visibility(visibility)
                .allowedFileFormats(Set.of(SubmissionFileFormat.PDF))
                .contest(contest)
                .build());
    }

    private void saveFile(final Long submissionId, final String name, final Long size) {
        fileDocumentRepository.save(FileDocument.builder()
                .file(File.create(name, "files/" + submissionId + "-" + name, "application/pdf", size))
                .submissionId(submissionId)
                .fileOrder(0)
                .build());
    }
}
