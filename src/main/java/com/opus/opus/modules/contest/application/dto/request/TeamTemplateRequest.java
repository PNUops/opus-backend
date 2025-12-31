package com.opus.opus.modules.contest.application.dto.request;

import com.opus.opus.modules.contest.domain.ContestTeamTemplateFieldType;
import jakarta.validation.constraints.NotNull;

public record TeamTemplateRequest(
        @NotNull(message = "분과 설정은 필수입니다.")
        ContestTeamTemplateFieldType division,
        @NotNull(message = "프로젝트명 설정은 필수입니다.")
        ContestTeamTemplateFieldType projectName,
        @NotNull(message = "팀명 설정은 필수입니다.")
        ContestTeamTemplateFieldType teamName,
        @NotNull(message = "팀장 설정은 필수입니다.")
        ContestTeamTemplateFieldType leader,
        @NotNull(message = "팀원 설정은 필수입니다.")
        ContestTeamTemplateFieldType teamMembers,
        @NotNull(message = "지도 교수 설정은 필수입니다.")
        ContestTeamTemplateFieldType professor,
        @NotNull(message = "GitHub 링크 설정은 필수입니다.")
        ContestTeamTemplateFieldType githubPath,
        @NotNull(message = "YouTube 링크 설정은 필수입니다.")
        ContestTeamTemplateFieldType youtubePath,
        @NotNull(message = "배포 링크 설정은 필수입니다.")
        ContestTeamTemplateFieldType productionPath,
        @NotNull(message = "프로젝트 개요 설정은 필수입니다.")
        ContestTeamTemplateFieldType overview,
        @NotNull(message = "포스터 설정은 필수입니다.")
        ContestTeamTemplateFieldType poster,
        @NotNull(message = "이미지 설정은 필수입니다.")
        ContestTeamTemplateFieldType images
) {
}

