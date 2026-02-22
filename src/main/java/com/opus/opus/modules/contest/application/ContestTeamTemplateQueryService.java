package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTeamTemplateConvenience;
import com.opus.opus.modules.contest.application.dto.response.TeamTemplateResponse;
import com.opus.opus.modules.contest.domain.ContestTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestTeamTemplateQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestTeamTemplateConvenience contestTeamTemplateConvenience;

    public TeamTemplateResponse getTeamTemplate(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final ContestTemplate template = contestTeamTemplateConvenience.getValidateExistTemplate(contestId);
        return TeamTemplateResponse.from(template);
    }
}
