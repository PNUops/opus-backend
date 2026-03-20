package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamAwardResult;
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
        List<AwardResponse> awards
) {

    public static TeamDetailResponse of(
            final Team team,
            final String contestName,
            final String trackName,
            final List<TeamMemberResponse> teamMembers,
            final List<Long> previewIds,
            final List<TeamAwardResult> teamAwardResults,
            final Boolean isVoted,
            final Boolean isLiked
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
                teamAwardResults.stream()
                        .map(result -> new AwardResponse(result.awardName(), result.awardColor()))
                        .toList()
        );
    }

    public record TeamMemberResponse(
            Long teamMemberId,
            String teamMemberName,
            TeamMemberRoleType roleType
    ) {
    }

    public record AwardResponse(
            String awardName,
            String awardColor
    ) {
    }
}
