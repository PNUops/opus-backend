package com.opus.opus.contest;

import com.opus.opus.modules.contest.application.dto.request.ContestTemplateRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTemplate;

public class ContestTemplateFixture {

    public static ContestTemplate createContestTemplate(final Contest contest) {
        final ContestTemplateRequest request = new ContestTemplateRequest(
                true, true, true, true, true, true,
                true, true, true, true, true, true
        );

        return ContestTemplate.builder()
                .contest(contest)
                .request(request)
                .build();
    }
}
