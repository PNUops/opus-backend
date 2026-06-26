package com.opus.opus.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.INVALID_SUBMISSION_FOR_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.NOT_FOUND_MEMO;
import static com.opus.opus.modules.team.domain.TeamMemberRoleType.ROLE_팀원;
import static com.opus.opus.modules.team.exception.TeamMemberExceptionType.TEAM_MEMBER_NOT_FOUND_IN_TEAM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.contest.ContestFixture;
import com.opus.opus.contest.ContestSubmissionItemFixture;
import com.opus.opus.helper.IntegrationTest;
import com.opus.opus.member.MemberFixture;
import com.opus.opus.modules.contest.application.ContestSubmissionMemoQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMemoResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionMemoRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoException;
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

class ContestSubmissionMemoQueryServiceTest extends IntegrationTest {

    @Autowired
    private ContestSubmissionMemoQueryService memoQueryService;

    @Autowired
    private ContestRepository contestRepository;
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
    @DisplayName("[성공] 팀원이 메모를 조회할 수 있다.")
    void 팀원이_메모를_조회할_수_있다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submission));

        // when
        final ContestSubmissionMemoResponse response =
                memoQueryService.getMemo(contest.getId(), team.getId(), submission.getId(), member);

        // then
        assertThat(response.content()).isEqualTo("테스트 메모 내용입니다.");
        assertThat(response.memoId()).isNotNull();
    }

    @Test
    @DisplayName("[실패] 메모가 없을 때 조회하면 NOT_FOUND_MEMO 예외가 발생한다.")
    void 메모가_없을_때_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() ->
                memoQueryService.getMemo(contest.getId(), team.getId(), submission.getId(), member))
                .isInstanceOf(ContestSubmissionMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionMemoException) e).exceptionType())
                        .isEqualTo(NOT_FOUND_MEMO));
    }

    @Test
    @DisplayName("[실패] 다른 대회의 contestId로 조회하면 INVALID_SUBMISSION_FOR_CONTEST 예외가 발생한다.")
    void 다른_대회의_contestId로_조회하면_예외가_발생한다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submission));
        final Contest otherContest = contestRepository.save(ContestFixture.createContestWithCategoryId(2L));

        // when & then
        assertThatThrownBy(() ->
                memoQueryService.getMemo(otherContest.getId(), team.getId(), submission.getId(), member))
                .isInstanceOf(ContestSubmissionMemoException.class)
                .satisfies(e -> assertThat(((ContestSubmissionMemoException) e).exceptionType())
                        .isEqualTo(INVALID_SUBMISSION_FOR_CONTEST));
    }

    @Test
    @DisplayName("[실패] 팀원이 아닌 사용자가 메모를 조회하면 TEAM_MEMBER_NOT_FOUND_IN_TEAM 예외가 발생한다.")
    void 팀원이_아닌_사용자가_메모를_조회하면_예외가_발생한다() {
        // given
        memoRepository.save(ContestSubmissionItemFixture.createMemo(submission));
        final Member outsider = memberRepository.save(MemberFixture.createMemberWithUniqueNum(9));

        // when & then
        assertThatThrownBy(() ->
                memoQueryService.getMemo(contest.getId(), team.getId(), submission.getId(), outsider))
                .isInstanceOf(TeamMemberException.class)
                .satisfies(e -> assertThat(((TeamMemberException) e).exceptionType())
                        .isEqualTo(TEAM_MEMBER_NOT_FOUND_IN_TEAM));
    }
}
