package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestMemberFixture.createContestMember;
import static com.opus.opus.contest.ContestSubmissionFeedbackFixture.createFeedback;
import static com.opus.opus.contest.ContestSubmissionFixture.createSubmission;
import static com.opus.opus.member.MemberFixture.createMemberWithRole;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestMemberExceptionType.NOT_ASSIGNED_TEAM;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_교수;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_외부멘토;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.modules.contest.application.ContestMentorQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFileResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectsResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorSubmissionResponse.FeedbackStatus;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.SubmissionVisibility;
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

public class ContestMentorQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestMentorQueryService contestMentorQueryService;
    @Autowired
    private ContestRepository contestRepository;
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

    private Contest contest;
    private ContestTrack track;
    private Member professor;
    private Team developTeam;
    private Team planningTeam;
    private ContestSubmission reviewedSubmission;
    private ContestSubmission pendingSubmission;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        track = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        professor = memberRepository.save(createMemberWithRole("김교수", 1, ROLE_교수));
        developTeam = teamRepository.save(buildTeam("개발 1팀"));
        planningTeam = teamRepository.save(buildTeam("운영 기획팀"));

        contestMemberRepository.save(
                createContestMember(contest, professor.getId(), List.of(developTeam.getId(), planningTeam.getId())));

        final ContestSubmissionItem midItem = saveItem("중간발표 자료", SubmissionVisibility.PUBLIC);
        final ContestSubmissionItem finalItem = saveItem("최종발표 자료", SubmissionVisibility.PUBLIC);
        final ContestSubmissionItem privateItem = saveItem("비공개 자료", SubmissionVisibility.STAFF);

        reviewedSubmission = contestSubmissionRepository.save(createSubmission(developTeam.getId(), midItem));
        pendingSubmission = contestSubmissionRepository.save(createSubmission(developTeam.getId(), finalItem));
        contestSubmissionRepository.save(createSubmission(developTeam.getId(), privateItem));

        contestSubmissionFeedbackRepository.save(createFeedback(reviewedSubmission, professor.getId()));
        saveFile(reviewedSubmission.getId(), "중간발표.pdf", 13002342L);
        saveFile(pendingSubmission.getId(), "최종발표.pdf", 20480L);
    }

    @Test
    @DisplayName("[성공] 담당 프로젝트 목록과 통계를 조회한다.")
    void 담당_프로젝트_목록과_통계를_조회한다() {
        final MentorProjectsResponse response = contestMentorQueryService.getMentorProjects(contest.getId(), professor);

        assertThat(response.assignedTeamCount()).isEqualTo(2);
        assertThat(response.pendingFeedbackCount()).isEqualTo(1);

        final MentorProjectResponse develop = findProject(response, developTeam.getId());
        assertThat(develop.projectName()).isEqualTo("옵스 프로젝트");
        assertThat(develop.trackName()).isEqualTo(track.getTrackName());
        assertThat(develop.roleType()).isEqualTo(ROLE_교수.name());
        assertThat(develop.pendingFeedbackCount()).isEqualTo(1);

        final MentorProjectResponse planning = findProject(response, planningTeam.getId());
        assertThat(planning.pendingFeedbackCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("[성공] 배정되지 않은 멘토는 빈 목록을 반환한다.")
    void 배정되지_않은_멘토는_빈_목록을_반환한다() {
        final Member unassigned = memberRepository.save(createMemberWithRole("이멘토", 2, ROLE_외부멘토));

        final MentorProjectsResponse response = contestMentorQueryService.getMentorProjects(contest.getId(), unassigned);

        assertThat(response.assignedTeamCount()).isEqualTo(0);
        assertThat(response.pendingFeedbackCount()).isEqualTo(0);
        assertThat(response.projects()).isEmpty();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 대회의 담당 프로젝트는 조회할 수 없다.")
    void 존재하지_않는_대회의_담당_프로젝트는_조회할_수_없다() {
        assertThatThrownBy(() -> contestMentorQueryService.getMentorProjects(999L, professor))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_FOUND_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 담당 팀의 공개 제출물과 피드백 상태를 조회한다.")
    void 담당_팀의_공개_제출물과_피드백_상태를_조회한다() {
        final TeamSubmissionsResponse response =
                contestMentorQueryService.getTeamSubmissions(contest.getId(), developTeam.getId(), professor);

        assertThat(response.teamId()).isEqualTo(developTeam.getId());
        assertThat(response.trackName()).isEqualTo(track.getTrackName());
        assertThat(response.pendingFeedbackCount()).isEqualTo(1);
        assertThat(response.submissions())
                .extracting(MentorSubmissionResponse::feedbackStatus)
                .containsExactly(FeedbackStatus.COMPLETED, FeedbackStatus.PENDING);

        final MentorSubmissionResponse reviewed = response.submissions().get(0);
        assertThat(reviewed.submissionItemName()).isEqualTo("중간발표 자료");
        assertThat(reviewed.files())
                .extracting(ContestSubmissionFileResponse::fileName, ContestSubmissionFileResponse::fileSize)
                .containsExactly(tuple("중간발표.pdf", 13002342L));
    }

    @Test
    @DisplayName("[실패] 본인 담당 팀이 아니면 조회할 수 없다.")
    void 본인_담당_팀이_아니면_조회할_수_없다() {
        final Member otherMentor = memberRepository.save(createMemberWithRole("박멘토", 3, ROLE_외부멘토));
        final Team otherTeam = teamRepository.save(buildTeam("보안 2팀"));
        contestMemberRepository.save(createContestMember(contest, otherMentor.getId(), List.of(otherTeam.getId())));

        assertThatThrownBy(() ->
                contestMentorQueryService.getTeamSubmissions(contest.getId(), developTeam.getId(), otherMentor))
                .isInstanceOf(ContestMemberException.class)
                .hasMessage(NOT_ASSIGNED_TEAM.errorMessage());
    }

    private MentorProjectResponse findProject(final MentorProjectsResponse response, final Long teamId) {
        return response.projects().stream()
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
