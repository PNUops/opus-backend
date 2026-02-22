package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestTemplateExceptionType.NOT_FOUND_TEMPLATE;

import com.opus.opus.modules.contest.domain.ContestTemplate;
import com.opus.opus.modules.contest.domain.dao.ContestTemplateRepository;
import com.opus.opus.modules.contest.exception.ContestTemplateException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestTemplateConvenience {

    private final ContestTemplateRepository contestTemplateRepository;

    public ContestTemplate getValidateExistTemplate(final Long contestId) {
        return contestTemplateRepository.findByContestId(contestId)
                .orElseThrow(() -> new ContestTemplateException(NOT_FOUND_TEMPLATE));
    }
}
