package com.opus.opus.modules.contest.application.dto.response;

import com.opus.opus.modules.contest.domain.ContestTeamTemplate;
import com.opus.opus.modules.contest.domain.ContestTeamTemplateFieldType;

public record TeamTemplateResponse(
        Long contestId,
        ContestTeamTemplateFieldType division,
        ContestTeamTemplateFieldType projectName,
        ContestTeamTemplateFieldType teamName,
        ContestTeamTemplateFieldType leader,
        ContestTeamTemplateFieldType teamMembers,
        ContestTeamTemplateFieldType professor,
        ContestTeamTemplateFieldType githubPath,
        ContestTeamTemplateFieldType youtubePath,
        ContestTeamTemplateFieldType productionPath,
        ContestTeamTemplateFieldType overview,
        ContestTeamTemplateFieldType poster,
        ContestTeamTemplateFieldType images
) {
    public static TeamTemplateResponse from(final ContestTeamTemplate template) {
        return new TeamTemplateResponse(
                template.getContest().getId(),
                template.getDivision(),
                template.getProjectName(),
                template.getTeamName(),
                template.getLeader(),
                template.getTeamMembers(),
                template.getProfessor(),
                template.getGithubPath(),
                template.getYoutubePath(),
                template.getProductionPath(),
                template.getOverview(),
                template.getPoster(),
                template.getImages()
        );
    }
}
