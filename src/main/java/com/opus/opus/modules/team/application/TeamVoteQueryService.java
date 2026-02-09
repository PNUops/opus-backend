package com.opus.opus.modules.team.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamVoteQueryService {

    private final ContestConvenience contestConvenience;

    private final TeamVoteRepository teamVoteRepository;

    @Transactional(readOnly = true)
    public MemberVoteCountResponse getMemberVoteCount(Long memberId, Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final long currentVoteCount = teamVoteRepository.countMemberVotesInContest(memberId, contestId);
        final long remainingVotesCount = contest.getMaxVotesLimit() - currentVoteCount;
        return new MemberVoteCountResponse(remainingVotesCount, (long) contest.getMaxVotesLimit());
    }
}
