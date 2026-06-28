package com.opus.opus.contest.application;

import static com.opus.opus.contest.ContestFixture.createContest;
import static com.opus.opus.contest.ContestSubmissionItemFixture.createSubmissionItem;
import static com.opus.opus.contest.ContestTrackFixture.createTrack;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_CONTEST_MEMBER;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀장;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionItemQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemSummaryResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.team.TeamFixture;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ContestSubmissionItemQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionItemQueryService contestSubmissionItemQueryService;

    @Autowired
    private ContestSubmissionItemRepository contestSubmissionItemRepository;
    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    private Contest contest;
    private ContestTrack track;
    private Member teamLeader;
    private Member teamMember;
    private Member admin;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(createContest());
        track = contestTrackRepository.save(createTrack(contest));

        final Team team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        teamLeader = saveTeamMember(team, MemberFixture.createMemberWithUniqueNum(1), ROLE_팀장);
        teamMember = saveTeamMember(team, MemberFixture.createMemberWithUniqueNum(2), ROLE_팀원);
        admin = memberRepository.save(Member.generalMember()
                .name("관리자")
                .email("admin@pusan.ac.kr")
                .password("{noop}12345678")
                .studentId("000000000")
                .roles(Set.of(ROLE_관리자))
                .build());
    }

    private Member saveTeamMember(final Team team, final Member member, final TeamMemberRoleType roleType) {
        final Member savedMember = memberRepository.save(member);
        teamMemberRepository.save(TeamMember.builder()
                .memberId(savedMember.getId())
                .team(team)
                .roles(Set.of(roleType))
                .build());
        return savedMember;
    }

    @Test
    @DisplayName("[성공] 팀장이 제출 항목의 설정값을 조회한다.")
    void 팀장이_제출_항목의_설정값을_조회한다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));

        final ContestSubmissionItemResponse response =
                contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), submissionItem.getId(), teamLeader);

        assertThat(response.name()).isEqualTo("발표자료");
        assertThat(response.contestTrackId()).isEqualTo(track.getId());
        assertThat(response.allowedFileFormats()).containsExactlyInAnyOrder("PDF", "ZIP");
        assertThat(response.maxFileSizeMb()).isEqualTo(50);
        assertThat(response.maxFileCount()).isEqualTo(3);
        assertThat(response.allowLateSubmission()).isTrue();
        assertThat(response.visibility()).isEqualTo("PUBLIC");
    }

    @Test
    @DisplayName("[성공] 팀원이 제출 항목의 설정값을 조회한다.")
    void 팀원이_제출_항목의_설정값을_조회한다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));

        final ContestSubmissionItemResponse response =
                contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), submissionItem.getId(), teamMember);

        assertThat(response.name()).isEqualTo("발표자료");
    }

    @Test
    @DisplayName("[성공] 관리자는 팀에 속하지 않아도 제출 항목을 조회한다.")
    void 관리자는_팀에_속하지_않아도_제출_항목을_조회한다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));

        final ContestSubmissionItemResponse response =
                contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), submissionItem.getId(), admin);

        assertThat(response.name()).isEqualTo("발표자료");
    }

    @Test
    @DisplayName("[실패] 해당 대회의 팀에 속하지 않은 회원이면 조회에 실패한다.")
    void 해당_대회의_팀에_속하지_않은_회원이면_조회에_실패한다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, track));
        final Member outsider = memberRepository.save(MemberFixture.createMemberWithUniqueNum(3));

        assertThatThrownBy(() -> contestSubmissionItemQueryService.getSubmissionItem(
                contest.getId(), submissionItem.getId(), outsider))
                .isInstanceOf(ContestException.class)
                .hasMessage(NOT_CONTEST_MEMBER.errorMessage());
    }

    @Test
    @DisplayName("[성공] 전체 분과 제출 항목은 분과 ID가 null로 조회된다.")
    void 전체_분과_제출_항목은_분과_ID가_null로_조회된다() {
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(contest, null));

        final ContestSubmissionItemResponse response =
                contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), submissionItem.getId(), teamLeader);

        assertThat(response.contestTrackId()).isNull();
    }

    @Test
    @DisplayName("[실패] 존재하지 않는 제출 항목이면 조회에 실패한다.")
    void 존재하지_않는_제출_항목이면_조회에_실패한다() {
        assertThatThrownBy(() -> contestSubmissionItemQueryService.getSubmissionItem(contest.getId(), 999L, teamLeader))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(NOT_FOUND_SUBMISSION_ITEM.errorMessage());
    }

    @Test
    @DisplayName("[실패] 다른 대회의 제출 항목이면 조회에 실패한다.")
    void 다른_대회의_제출_항목이면_조회에_실패한다() {
        final Contest otherContest = contestRepository.save(createContest());
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemRepository.save(createSubmissionItem(otherContest, null));

        assertThatThrownBy(() -> contestSubmissionItemQueryService.getSubmissionItem(
                contest.getId(), submissionItem.getId(), teamLeader))
                .isInstanceOf(ContestSubmissionItemException.class)
                .hasMessage(INVALID_SUBMISSION_ITEM_FOR_CONTEST.errorMessage());
    }

    @Test
    @DisplayName("[성공] 제출 항목 목록을 최신 수정 순으로 조회한다.")
    void 제출_항목_목록을_최신_수정_순으로_조회한다() {
        final LocalDateTime now = LocalDateTime.now();
        final ContestSubmissionItem first = contestSubmissionItemRepository.saveAndFlush(
                createSubmissionItem(contest, track, "발표자료", now.minusDays(1), now.plusDays(1)));
        final ContestSubmissionItem second = contestSubmissionItemRepository.saveAndFlush(
                createSubmissionItem(contest, track, "포스터", now.minusDays(1), now.plusDays(1)));

        final List<ContestSubmissionItemSummaryResponse> responses =
                contestSubmissionItemQueryService.getSubmissionItems(contest.getId(), null);

        assertThat(responses).extracting(ContestSubmissionItemSummaryResponse::contestSubmissionItemId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    @DisplayName("[성공] 시작 전 제출 항목은 운영 상태가 SCHEDULED로 조회된다.")
    void 시작_전_제출_항목은_운영_상태가_SCHEDULED로_조회된다() {
        final LocalDateTime now = LocalDateTime.now();
        contestSubmissionItemRepository.save(
                createSubmissionItem(contest, track, "발표자료", now.plusDays(1), now.plusDays(2)));

        final List<ContestSubmissionItemSummaryResponse> responses =
                contestSubmissionItemQueryService.getSubmissionItems(contest.getId(), null);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).operationStatus()).isEqualTo("SCHEDULED");
    }

    @Test
    @DisplayName("[성공] 진행 중인 제출 항목은 운영 상태가 IN_PROGRESS로 조회된다.")
    void 진행_중인_제출_항목은_운영_상태가_IN_PROGRESS로_조회된다() {
        final LocalDateTime now = LocalDateTime.now();
        contestSubmissionItemRepository.save(
                createSubmissionItem(contest, track, "발표자료", now.minusDays(1), now.plusDays(1)));

        final List<ContestSubmissionItemSummaryResponse> responses =
                contestSubmissionItemQueryService.getSubmissionItems(contest.getId(), null);

        assertThat(responses.get(0).operationStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    @DisplayName("[성공] 마감된 제출 항목은 운영 상태가 CLOSED로 조회된다.")
    void 마감된_제출_항목은_운영_상태가_CLOSED로_조회된다() {
        final LocalDateTime now = LocalDateTime.now();
        contestSubmissionItemRepository.save(
                createSubmissionItem(contest, track, "발표자료", now.minusDays(2), now.minusDays(1)));

        final List<ContestSubmissionItemSummaryResponse> responses =
                contestSubmissionItemQueryService.getSubmissionItems(contest.getId(), null);

        assertThat(responses.get(0).operationStatus()).isEqualTo("CLOSED");
    }

    @Test
    @DisplayName("[성공] 운영 상태로 필터링하여 제출 항목 목록을 조회한다.")
    void 운영_상태로_필터링하여_제출_항목_목록을_조회한다() {
        final LocalDateTime now = LocalDateTime.now();
        contestSubmissionItemRepository.save(
                createSubmissionItem(contest, track, "진행중", now.minusDays(1), now.plusDays(1)));
        contestSubmissionItemRepository.save(
                createSubmissionItem(contest, track, "마감", now.minusDays(2), now.minusDays(1)));

        final List<ContestSubmissionItemSummaryResponse> responses =
                contestSubmissionItemQueryService.getSubmissionItems(contest.getId(), "IN_PROGRESS");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).name()).isEqualTo("진행중");
    }

    @Test
    @DisplayName("[성공] 전체 분과 제출 항목은 분과명이 null로 조회된다.")
    void 전체_분과_제출_항목은_분과명이_null로_조회된다() {
        final LocalDateTime now = LocalDateTime.now();
        contestSubmissionItemRepository.save(
                createSubmissionItem(contest, null, "발표자료", now.minusDays(1), now.plusDays(1)));

        final List<ContestSubmissionItemSummaryResponse> responses =
                contestSubmissionItemQueryService.getSubmissionItems(contest.getId(), null);

        assertThat(responses.get(0).trackName()).isNull();
    }
}
