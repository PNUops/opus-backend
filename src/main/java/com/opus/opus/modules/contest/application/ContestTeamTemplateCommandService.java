package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTeamTemplateConvenience;
import com.opus.opus.modules.contest.application.dto.request.TeamTemplateRequest;
import com.opus.opus.modules.contest.domain.ContestTeamTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestTeamTemplateCommandService {

    private final ContestConvenience contestConvenience;
    private final ContestTeamTemplateConvenience contestTeamTemplateConvenience;

    public void updateTemplate(final Long contestId, final TeamTemplateRequest request) {

        contestConvenience.getValidateExistContest(contestId);
        final ContestTeamTemplate template = contestTeamTemplateConvenience.getValidateExistTemplate(
                contestId);

        template.updateTemplate(
                request.division(),
                request.projectName(),
                request.teamName(),
                request.leader(),
                request.teamMembers(),
                request.professor(),
                request.githubPath(),
                request.youtubePath(),
                request.productionPath(),
                request.overview(),
                request.poster(),
                request.images()
        );
    }
}
