package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionMemoExceptionType.MEMO_ALREADY_EXISTS;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionMemoConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionMemoRequest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionMemoRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionMemoException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestSubmissionMemoCommandService {

    private final ContestSubmissionMemoRepository memoRepository;

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionItemConvenience submissionItemConvenience;
    private final ContestSubmissionMemoConvenience memoConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;

    public void createMemo(final Long contestId, final Long teamId, final Long submissionItemId,
                           final ContestSubmissionMemoRequest request, final Member member) {
        contestConvenience.validateExistContest(contestId);
        final Team team = teamConvenience.getValidateTeamInContest(teamId, contestId);
        final ContestSubmissionItem submissionItem =
                submissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        // 메모는 제출물이 없어도 작성할 수 있어, 제출물 대신 팀 분과의 제출 항목인지 직접 확인한다 (#257)
        submissionItemConvenience.validateSubmissionItemInTrack(submissionItem, team.getTrackId());
        validateMemoNotAlreadyWritten(submissionItemId, teamId);

        memoRepository.save(ContestSubmissionMemo.builder()
                .content(request.content())
                .teamId(teamId)
                .submissionItem(submissionItem)
                .build());
    }

    public void updateMemo(final Long contestId, final Long teamId, final Long submissionItemId,
                           final ContestSubmissionMemoRequest request, final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        submissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionMemo memo = memoConvenience.getValidateExistMemo(submissionItemId, teamId);

        memo.updateContent(request.content());
    }

    public void deleteMemo(final Long contestId, final Long teamId, final Long submissionItemId,
                           final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        submissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionMemo memo = memoConvenience.getValidateExistMemo(submissionItemId, teamId);

        memoRepository.delete(memo);
    }

    private void validateMemoNotAlreadyWritten(final Long submissionItemId, final Long teamId) {
        if (memoConvenience.isMemoWritten(submissionItemId, teamId)) {
            throw new ContestSubmissionMemoException(MEMO_ALREADY_EXISTS);
        }
    }
}
