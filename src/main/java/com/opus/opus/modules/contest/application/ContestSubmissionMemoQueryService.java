package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionMemoConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMemoResponse;
import com.opus.opus.modules.contest.domain.ContestSubmissionMemo;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionMemoQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionItemConvenience submissionItemConvenience;
    private final ContestSubmissionMemoConvenience memoConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;

    public ContestSubmissionMemoResponse getMemo(final Long contestId, final Long teamId,
                                                 final Long submissionItemId, final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        submissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);

        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final ContestSubmissionMemo memo = memoConvenience.getValidateExistMemo(submissionItemId, teamId);

        return ContestSubmissionMemoResponse.from(memo);
    }
}
