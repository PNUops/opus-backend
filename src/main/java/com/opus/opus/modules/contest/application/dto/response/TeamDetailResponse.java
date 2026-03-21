package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse.AwardInfo;
import com.opus.opus.modules.team.domain.Team;
import java.util.List;

public record TeamDetailResponse(
        Long contestId,
        String contestName,
        Long trackId,
        String trackName,
        Long teamId,
        String teamName,
        String projectName,
        List<TeamMemberResponse> teamMembers,
        String professorName,
        String githubPath,
        String youTubePath,
        String productionPath,
        String overview,
        List<Long> previewIds,
        Boolean isLiked,
        Boolean isVoted,
        List<AwardInfo> awards
) {

    public static TeamDetailResponse of(
            final Team team,
            final String contestName,
            final String trackName,
            final List<TeamMemberResponse> teamMembers,
            final List<Long> previewIds,
            final List<AwardInfo> awards,
            final Boolean isLiked,
            final Boolean isVoted
    ) {
        return new TeamDetailResponse(
                team.getContestId(),
                contestName,
                team.getTrackId(),
                trackName,
                team.getId(),
                team.getTeamName(),
                team.getProjectName(),
                teamMembers,
                team.getProfessorName(),
                team.getGithubPath(),
                team.getYouTubePath(),
                team.getProductionPath(),
                team.getOverview(),
                previewIds,
                isLiked,
                isVoted,
                awards
        );
    }
}
