package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.domain.ContestTeamTemplateFieldType.HIDDEN;
import static com.opus.opus.modules.contest.domain.ContestTeamTemplateFieldType.OPTIONAL;
import static com.opus.opus.modules.contest.domain.ContestTeamTemplateFieldType.REQUIRED;
import static com.opus.opus.modules.contest.exception.ContestTeamTemplateExceptionType.NOT_FOUND_TEMPLATE;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTeamTemplate;
import com.opus.opus.modules.contest.domain.ContestTeamTemplateFieldType;
import com.opus.opus.modules.contest.domain.dao.ContestTeamTemplateRepository;
import com.opus.opus.modules.contest.exception.ContestTeamTemplateException;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestTeamTemplateConvenience {

    private final ContestTeamTemplateRepository contestTeamTemplateRepository;

    public ContestTeamTemplate getValidateExistTemplate(final Long contestId) {
        return contestTeamTemplateRepository.findByContestId(contestId)
                .orElseThrow(() -> new ContestTeamTemplateException(NOT_FOUND_TEMPLATE));
    }

    public void createTemplate(final Contest contest, final String categoryName) {
        final Map<String, ContestTeamTemplateFieldType> settings = getTemplateDefaultSettings(categoryName);

        ContestTeamTemplate template = ContestTeamTemplate.builder()
                .contest(contest)
                .division(settings.get("division"))
                .projectName(settings.get("projectName"))
                .teamName(settings.get("teamName"))
                .leader(settings.get("leader"))
                .teamMembers(settings.get("teamMembers"))
                .professor(settings.get("professor"))
                .githubPath(settings.get("githubPath"))
                .youtubePath(settings.get("youtubePath"))
                .productionPath(settings.get("productionPath"))
                .overview(settings.get("overview"))
                .poster(settings.get("poster"))
                .images(settings.get("images"))
                .build();

        contestTeamTemplateRepository.save(template);
    }

    private Map<String, ContestTeamTemplateFieldType> getTemplateDefaultSettings(final String categoryName) {

        Map<String, ContestTeamTemplateFieldType> map = new HashMap<>();

        if (categoryName.contains("창의융합")) {
            map.put("division", REQUIRED);
            map.put("projectName", REQUIRED);
            map.put("teamName", REQUIRED);
            map.put("leader", REQUIRED);
            map.put("teamMembers", REQUIRED);
            map.put("professor", HIDDEN);
            map.put("githubPath", REQUIRED);
            map.put("youtubePath", OPTIONAL);
            map.put("productionPath", OPTIONAL);
            map.put("overview", REQUIRED);
            map.put("poster", REQUIRED);
            map.put("images", REQUIRED);

        } else if (categoryName.contains("캡스톤")) {
            map.put("division", REQUIRED);
            map.put("projectName", REQUIRED);
            map.put("teamName", REQUIRED);
            map.put("leader", REQUIRED);
            map.put("teamMembers", REQUIRED);
            map.put("professor", REQUIRED);
            map.put("githubPath", REQUIRED);
            map.put("youtubePath", REQUIRED);
            map.put("productionPath", OPTIONAL);
            map.put("overview", REQUIRED);
            map.put("poster", OPTIONAL);
            map.put("images", REQUIRED);

        } else {
            map.put("division", OPTIONAL);
            map.put("projectName", OPTIONAL);
            map.put("teamName", OPTIONAL);
            map.put("leader", OPTIONAL);
            map.put("teamMembers", OPTIONAL);
            map.put("professor", OPTIONAL);
            map.put("githubPath", OPTIONAL);
            map.put("youtubePath", OPTIONAL);
            map.put("productionPath", OPTIONAL);
            map.put("overview", OPTIONAL);
            map.put("poster", OPTIONAL);
            map.put("images", OPTIONAL);
        }
        return map;
    }

}
