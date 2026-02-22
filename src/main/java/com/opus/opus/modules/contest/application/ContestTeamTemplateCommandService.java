package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTeamTemplateConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestTemplateRequest;
import com.opus.opus.modules.contest.domain.ContestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestTeamTemplateCommandService {

    private final ContestConvenience contestConvenience;
    private final ContestTeamTemplateConvenience contestTeamTemplateConvenience;

    public void updateTeamTemplate(final Long contestId, final ContestTemplateRequest request) {

        contestConvenience.getValidateExistContest(contestId);
        final ContestTemplate template = contestTeamTemplateConvenience.getValidateExistTemplate(
                contestId);

        template.updateTemplate(
                request.divisionRequired(),
                request.projectNameRequired(),
                request.teamNameRequired(),
                request.leaderRequired(),
                request.teamMembersRequired(),
                request.professorRequired(),
                request.githubPathRequired(),
                request.youtubePathRequired(),
                request.productionPathRequired(),
                request.overviewRequired(),
                request.posterRequired(),
                request.imagesRequired()
        );
    }
}
