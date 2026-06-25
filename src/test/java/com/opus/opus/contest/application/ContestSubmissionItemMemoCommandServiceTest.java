package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.INVALID_SUBMISSION_ITEM_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.MEMO_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoExceptionType.NOT_FOUND_MEMO;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionItemMemoCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemMemoRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestSubmissionItemMemo;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemMemoRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemMemoException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamMemberException;
import com.opus.opus.team.TeamFixture;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ContestSubmissionItemMemoCommandServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionItemMemoCommandService memoCommandService;

    @Autowired
    private ContestRepository contestRepository;
    @Autowired
    private ContestSubmissionItemRepository submissionItemRepository;
    @Autowired
    private ContestSubmissionItemMemoRepository memoRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private MemberRepository memberRepository;

    private Contest contest;
    private ContestSubmissionItem submissionItem;
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
    }

    @Test
    @DisplayName("[성공] 팀원이 메모를 생성할 수 있다.")
    void 팀원이_메모를_생성할_수_있다() {
        // given
        final ContestSubmissionItemMemoRequest request = new ContestSubmissionItemMemoRequest("새 메모 내용");

        // when
        memoCommandService.createMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member);

        // then
        assertThat(memoRepository.findBySubmissionItemId(submissionItem.getId())).isPresent();
    }

    @Test
    @DisplayName("[실패] 이미 메모가 존재하면 생성 시 MEMO_ALREADY_EXISTS 예외가 발생한다.")
    void 이미_메모가_존재하면_생성_시_예외가_발생한다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem));
        final ContestSubmissionItemMemoRequest request = new ContestSubmissionItemMemoRequest("중복 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member))
                .isInstanceOf(ContestSubmissionItemMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionItemMemoException) e).exceptionType())
                        .isEqualTo(MEMO_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("[실패] 팀원이 아닌 사용자가 메모를 생성하면 TEAM_MEMBER_NOT_FOUND_IN_TEAM 예외가 발생한다.")
    void 팀원이_아닌_사용자가_메모를_생성하면_예외가_발생한다() {
        // given
        final Member outsider = memberRepository.save(MemberFixture.createMemberWithUniqueNum(9));
        final ContestSubmissionItemMemoRequest request = new ContestSubmissionItemMemoRequest("외부인 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(contest.getId(), team.getId(), submissionItem.getId(), request, outsider))
                .isInstanceOf(TeamMemberException.class)
                .satisfies(e -> assertThat(((TeamMemberException) e).exceptionType())
                        .isEqualTo(TEAM_MEMBER_NOT_FOUND_IN_TEAM));
    }

    @Test
    @DisplayName("[실패] 대회에 속하지 않는 제출 항목으로 생성하면 INVALID_SUBMISSION_ITEM_FOR_CONTEST 예외가 발생한다.")
    void 다른_대회의_제출항목으로_생성하면_예외가_발생한다() {
        // given
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(2L));
        final ContestSubmissionItemMemoRequest request = new ContestSubmissionItemMemoRequest("잘못된 메모");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.createMemo(otherContest.getId(), team.getId(), submissionItem.getId(), request, member))
                .isInstanceOf(ContestSubmissionItemMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionItemMemoException) e).exceptionType())
                        .isEqualTo(INVALID_SUBMISSION_ITEM_FOR_CONTEST));
    }

    @Test
    @DisplayName("[성공] 팀원이 메모를 수정할 수 있다.")
    void 팀원이_메모를_수정할_수_있다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem));
        final ContestSubmissionItemMemoRequest request = new ContestSubmissionItemMemoRequest("수정된 메모 내용");

        // when
        memoCommandService.updateMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member);

        // then
        final ContestSubmissionItemMemo updated = memoRepository.findBySubmissionItemId(submissionItem.getId()).orElseThrow();
        assertThat(updated.getContent()).isEqualTo("수정된 메모 내용");
    }

    @Test
    @DisplayName("[실패] 메모가 없을 때 수정하면 NOT_FOUND_MEMO 예외가 발생한다.")
    void 메모가_없을_때_수정하면_예외가_발생한다() {
        // given
        final ContestSubmissionItemMemoRequest request = new ContestSubmissionItemMemoRequest("수정 시도");

        // when & then
        assertThatThrownBy(() ->
                memoCommandService.updateMemo(contest.getId(), team.getId(), submissionItem.getId(), request, member))
                .isInstanceOf(ContestSubmissionItemMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionItemMemoException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_MEMO));
    }

    @Test
    @DisplayName("[성공] 팀원이 메모를 삭제할 수 있다.")
    void 팀원이_메모를_삭제할_수_있다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submissionItem));

        // when
        memoCommandService.deleteMemo(contest.getId(), team.getId(), submissionItem.getId(), member);

        // then
        assertThat(memoRepository.findBySubmissionItemId(submissionItem.getId())).isEmpty();
    }

    @Test
    @DisplayName("[실패] 메모가 없을 때 삭제하면 NOT_FOUND_MEMO 예외가 발생한다.")
    void 메모가_없을_때_삭제하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() ->
                memoCommandService.deleteMemo(contest.getId(), team.getId(), submissionItem.getId(), member))
                .isInstanceOf(ContestSubmissionItemMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionItemMemoException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_MEMO));
    }
}
