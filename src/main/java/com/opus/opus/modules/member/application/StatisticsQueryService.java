package com.opus.opus.modules.member.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.member.application.dto.response.StatisticsSummaryResponse;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamLikeConvenience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsQueryService {

    private final TeamConvenience teamConvenience;
    private final TeamLikeConvenience teamLikeConvenience;
    private final ContestConvenience contestConvenience;

    public StatisticsSummaryResponse getStatisticsSummary() {
        final long totalProjects = teamConvenience.countSubmittedTeams();
        final long totalLikes = teamLikeConvenience.countAllLikes();
        final long totalContests = contestConvenience.countAllContests();
        return new StatisticsSummaryResponse(totalProjects, totalLikes, totalContests);
    }
}
