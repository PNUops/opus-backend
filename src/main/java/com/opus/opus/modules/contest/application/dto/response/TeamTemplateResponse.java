package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestTeamTemplate;

public record TeamTemplateResponse(
        Boolean divisionRequired,
        Boolean projectNameRequired,
        Boolean teamNameRequired,
        Boolean leaderRequired,
        Boolean teamMembersRequired,
        Boolean professorRequired,
        Boolean githubPathRequired,
        Boolean youtubePathRequired,
        Boolean productionPathRequired,
        Boolean overviewRequired,
        Boolean posterRequired,
        Boolean imagesRequired
) {
    public static TeamTemplateResponse from(final ContestTeamTemplate template) {
        return new TeamTemplateResponse(
                template.getDivisionRequired(),
                template.getProjectNameRequired(),
                template.getTeamNameRequired(),
                template.getLeaderRequired(),
                template.getTeamMembersRequired(),
                template.getProfessorRequired(),
                template.getGithubPathRequired(),
                template.getYoutubePathRequired(),
                template.getProductionPathRequired(),
                template.getOverviewRequired(),
                template.getPosterRequired(),
                template.getImagesRequired()
        );
    }
}
