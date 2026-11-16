package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_ITEM_FOR_TRACK;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.MEMO_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.NOT_FOUND_MEMO;
import static com.opus.opus.modules.member.domain.MemberRoleType.ROLE_관리자;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamExceptionType.TEAM_NOT_IN_CONTEST;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.contest.ContestTrackFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionMemoCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionMemoRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionMemoRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.team.TeamFixture;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ContestSubmissionMemoCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionMemoCommandService memoCommandService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestTrackRepository contestTrackRepository;
    @Autowired
    private ContestSubmissionItemRepository submissionItemRepository;
    @Autowired
    private ContestSubmissionRepository submissionRepository;
    @Autowired
    private ContestSubmissionMemoRepository memoRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Contest contest;
    private ContestSubmissionItem submissionItem;
    private ContestSubmission submission;
    private Team team;
    private Member member;

    @BeforeEach
    void setUp() {
        contest = contestRepository.save(ContestFixture.createContest());
        submissionItem = submissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));
        team = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        member = memberRepository.save(MemberFixture.createMember());
        teamMemberRepository.save(TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(ROLE_팀원))
                .build());
        submission = submissionRepository.save(
                ContestSubmissionItemFixture.createSubmission(submissionItem, team.getId()));
    }

    @Test
    @DisplayName("[성공] 팀원이 메모를 생성할 수 있다.")
    void 팀원이_메모를_생성할_수_있다() {
        // given
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("새 메모 내용");

        // when
        memoCommandService.createMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member);

        // then
        assertThat(findMemo()).isPresent();
    }

    @Test
    @DisplayName("[성공] 제출물이 없어도 메모를 생성할 수 있다.")
    void 제출물이_없어도_메모를_생성할_수_있다() {
        // given
        final ContestSubmissionItem notSubmittedItem =
                submissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(contest));
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("제출 전 메모");

        // when
        memoCommandService.createMemo(contest.getId(), team.getId(), notSubmittedItem.getId(), request, member);

        // then
        final ContestSubmissionMemo memo =
                memoRepository.findBySubmissionItemIdAndTeamId(notSubmittedItem.getId(), team.getId()).orElseThrow();
        assertThat(memo.getContent()).isEqualTo("제출 전 메모");
    }

    @Test
    @DisplayName("[성공] 같은 제출 항목이라도 다른 팀은 메모를 따로 생성할 수 있다.")
    void 같은_제출_항목이라도_다른_팀은_메모를_따로_생성할_수_있다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem, team.getId()));
        final Team otherTeam = teamRepository.save(TeamFixture.createTeamWithContestId(contest.getId()));
        final Member admin = memberRepository.save(MemberFixture.createMemberWithRole("관리자", 9, ROLE_관리자));
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("다른 팀 메모");

        // when
        memoCommandService.createMemo(contest.getId(), otherTeam.getId(), submissionItem.getId(), request, admin);

        // then
        assertThat(findMemo().orElseThrow().getContent()).isEqualTo("테스트 메모 내용입니다.");
        assertThat(memoRepository.findBySubmissionItemIdAndTeamId(submissionItem.getId(), otherTeam.getId())
                .orElseThrow().getContent()).isEqualTo("다른 팀 메모");
    }

    @Test
    @DisplayName("[실패] 이미 메모가 존재하면 생성 시 MEMO_ALREADY_EXISTS 예외가 발생한다.")
    void 이미_메모가_존재하면_생성_시_예외가_발생한다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem, team.getId()));
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("중복 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member))
                .isInstanceOf(ContestSubmissionMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionMemoException) e).exceptionType())
                        .isEqualTo(MEMO_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("[실패] 팀원이 아닌 사용자가 메모를 생성하면 TEAM_MEMBER_NOT_FOUND_IN_TEAM 예외가 발생한다.")
    void 팀원이_아닌_사용자가_메모를_생성하면_예외가_발생한다() {
        // given
        final Member outsider = memberRepository.save(MemberFixture.createMemberWithUniqueNum(9));
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("외부인 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(contest.getId(), team.getId(), submissionItem.getId(), request, outsider))
                .isInstanceOf(TeamMemberException.class)
                .satisfies(e -> assertThat(((TeamMemberException) e).exceptionType())
                        .isEqualTo(TEAM_MEMBER_NOT_FOUND_IN_TEAM));
    }

    @Test
    @DisplayName("[실패] 다른 대회의 contestId로 생성하면 TEAM_NOT_IN_CONTEST 예외가 발생한다.")
    void 다른_대회의_contestId로_생성하면_TEAM_NOT_IN_CONTEST_예외가_발생한다() {
        // given
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(2L));
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("잘못된 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(otherContest.getId(), team.getId(), submissionItem.getId(), request,
                        member))
                .isInstanceOf(TeamException.class)
                .satisfies(e -> assertThat(((TeamException) e).exceptionType())
                        .isEqualTo(TEAM_NOT_IN_CONTEST));
    }

    @Test
    @DisplayName("[실패] 다른 대회의 제출 항목으로 생성하면 INVALID_SUBMISSION_ITEM_FOR_CONTEST 예외가 발생한다.")
    void 다른_대회의_제출_항목으로_생성하면_INVALID_SUBMISSION_ITEM_FOR_CONTEST_예외가_발생한다() {
        // given
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(2L));
        final ContestSubmissionItem otherContestItem =
                submissionItemRepository.save(ContestSubmissionItemFixture.createSubmissionItem(otherContest));
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("잘못된 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(contest.getId(), team.getId(), otherContestItem.getId(), request,
                        member))
                .isInstanceOf(ContestSubmissionItemException.class)
                .satisfies(e -> assertThat(((ContestSubmissionItemException) e).exceptionType())
                        .isEqualTo(INVALID_SUBMISSION_ITEM_FOR_CONTEST));
    }

    @Test
    @DisplayName("[실패] 팀의 분과가 아닌 제출 항목으로 생성하면 INVALID_SUBMISSION_ITEM_FOR_TRACK 예외가 발생한다.")
    void 팀의_분과가_아닌_제출_항목으로_생성하면_INVALID_SUBMISSION_ITEM_FOR_TRACK_예외가_발생한다() {
        // given
        final ContestTrack teamTrack = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final ContestTrack otherTrack = contestTrackRepository.save(ContestTrackFixture.createTrack(contest));
        final Team trackTeam = teamRepository.save(
                TeamFixture.createTeamWithContestIdAndTrackId(contest.getId(), teamTrack.getId()));
        final ContestSubmissionItem otherTrackItem = submissionItemRepository.save(
                ContestSubmissionItemFixture.createSubmissionItem(contest, otherTrack));
        final Member admin = memberRepository.save(MemberFixture.createMemberWithRole("관리자", 9, ROLE_관리자));
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("다른 분과 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(contest.getId(), trackTeam.getId(), otherTrackItem.getId(), request,
                        admin))
                .isInstanceOf(ContestException.class)
                .satisfies(e -> assertThat(((ContestException) e).exceptionType())
                        .isEqualTo(INVALID_SUBMISSION_ITEM_FOR_TRACK));
    }

    @Test
    @DisplayName("[성공] 팀원이 메모를 수정할 수 있다.")
    void 팀원이_메모를_수정할_수_있다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem, team.getId()));
        assertThat(findMemo().orElseThrow().getContent()).isEqualTo("테스트 메모 내용입니다."); // 변경 전 값 검증
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("수정된 메모 내용");

        // when
        memoCommandService.updateMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member);

        // then
        final ContestSubmissionMemo updated = findMemo().orElseThrow();
        assertThat(updated.getContent()).isEqualTo("수정된 메모 내용");
    }

    @Test
    @DisplayName("[성공] 제출물이 삭제돼도 남아 있는 메모를 수정할 수 있다.")
    void 제출물이_삭제돼도_남아_있는_메모를_수정할_수_있다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem, team.getId()));
        submissionRepository.delete(submission);
        assertThat(findMemo().orElseThrow().getContent()).isEqualTo("테스트 메모 내용입니다."); // 변경 전 값 검증
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("수정된 메모 내용");

        // when
        memoCommandService.updateMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member);

        // then
        final ContestSubmissionMemo updated = findMemo().orElseThrow();
        assertThat(updated.getContent()).isEqualTo("수정된 메모 내용");
    }

    @Test
    @DisplayName("[실패] 메모가 없을 때 수정하면 NOT_FOUND_MEMO 예외가 발생한다.")
    void 메모가_없을_때_수정하면_예외가_발생한다() {
        // given
        final ContestSubmissionMemoRequest request = new ContestSubmissionMemoRequest("수정 시도");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.updateMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member))
                .isInstanceOf(ContestSubmissionMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionMemoException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_MEMO));
    }

    @Test
    @DisplayName("[성공] 팀원이 메모를 삭제할 수 있다.")
    void 팀원이_메모를_삭제할_수_있다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem, team.getId()));

        // when
        memoCommandService.deleteMemo(contest.getId(), team.getId(), submissionItem.getId(), member);

        // then
        assertThat(findMemo()).isEmpty();
    }

    @Test
    @DisplayName("[실패] 메모가 없을 때 삭제하면 NOT_FOUND_MEMO 예외가 발생한다.")
    void 메모가_없을_때_삭제하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() ->
                memoCommandService.deleteMemo(contest.getId(), team.getId(), submissionItem.getId(), member))
                .isInstanceOf(ContestSubmissionMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionMemoException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_MEMO));
    }

    private Optional<ContestSubmissionMemo> findMemo() {
        return memoRepository.findBySubmissionItemIdAndTeamId(submissionItem.getId(), team.getId());
    }
}
