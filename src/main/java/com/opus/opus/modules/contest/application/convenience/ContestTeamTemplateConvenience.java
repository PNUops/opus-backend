package com.opus.opus.modules.contest.application.convenience;

import static com.opus.opus.modules.contest.exception.ContestTeamTemplateExceptionType.NOT_FOUND_TEMPLATE;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTemplate;
import com.opus.opus.modules.contest.domain.dao.ContestTemplateRepository;
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

    private final ContestTemplateRepository contestTemplateRepository;

    public ContestTemplate getValidateExistTemplate(final Long contestId) {
        return contestTemplateRepository.findByContestId(contestId)
                .orElseThrow(() -> new ContestTeamTemplateException(NOT_FOUND_TEMPLATE));
    }

    public void createTemplate(final Contest contest, final String categoryName) {
        final Map<String, Boolean> settings = getTemplateDefaultSettings(categoryName);

        ContestTemplate template = ContestTemplate.builder()
                .contest(contest)
                .divisionRequired(settings.get("division"))
                .projectNameRequired(settings.get("projectName"))
                .teamNameRequired(settings.get("teamName"))
                .leaderRequired(settings.get("leader"))
                .teamMembersRequired(settings.get("teamMembers"))
                .professorRequired(settings.get("professor"))
                .githubPathRequired(settings.get("githubPath"))
                .youtubePathRequired(settings.get("youtubePath"))
                .productionPathRequired(settings.get("productionPath"))
                .overviewRequired(settings.get("overview"))
                .posterRequired(settings.get("poster"))
                .imagesRequired(settings.get("images"))
                .build();

        contestTemplateRepository.save(template);
    }

    private Map<String, Boolean> getTemplateDefaultSettings(final String categoryName) {
        Map<String, Boolean> map = new HashMap<>();

        if (categoryName.contains("창의융합")) {
            map.put("division", true);
            map.put("projectName", true);
            map.put("teamName", true);
            map.put("leader", true);
            map.put("teamMembers", true);
            map.put("professor", false);
            map.put("githubPath", true);
            map.put("youtubePath", false);
            map.put("productionPath", false);
            map.put("overview", true);
            map.put("poster", true);
            map.put("images", true);

        } else if (categoryName.contains("캡스톤")) {
            map.put("division", true);
            map.put("projectName", true);
            map.put("teamName", true);
            map.put("leader", true);
            map.put("teamMembers", true);
            map.put("professor", true);
            map.put("githubPath", true);
            map.put("youtubePath", true);
            map.put("productionPath", false);
            map.put("overview", true);
            map.put("poster", false);
            map.put("images", true);

        } else {
            map.put("division", false);
            map.put("projectName", false);
            map.put("teamName", false);
            map.put("leader", false);
            map.put("teamMembers", false);
            map.put("professor", false);
            map.put("githubPath", false);
            map.put("youtubePath", false);
            map.put("productionPath", false);
            map.put("overview", false);
            map.put("poster", false);
            map.put("images", false);
        }
        return map;
    }

}
