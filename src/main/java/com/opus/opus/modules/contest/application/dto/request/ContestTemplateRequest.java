package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

public record ContestTemplateRequest(
        @NotNull(message = "분과 필수 여부는 필수입니다.")
        Boolean trackRequired,
        @NotNull(message = "프로젝트명 필수 여부는 필수입니다.")
        Boolean projectNameRequired,
        @NotNull(message = "팀명 필수 여부는 필수입니다.")
        Boolean teamNameRequired,
        @NotNull(message = "팀장 필수 여부는 필수입니다.")
        Boolean leaderRequired,
        @NotNull(message = "팀원 필수 여부는 필수입니다.")
        Boolean teamMembersRequired,
        @NotNull(message = "지도 교수 필수 여부는 필수입니다.")
        Boolean professorRequired,
        @NotNull(message = "GitHub 링크 필수 여부는 필수입니다.")
        Boolean githubPathRequired,
        @NotNull(message = "YouTube 링크 필수 여부는 필수입니다.")
        Boolean youTubePathRequired,
        @NotNull(message = "배포 링크 필수 여부는 필수입니다.")
        Boolean productionPathRequired,
        @NotNull(message = "프로젝트 개요 필수 여부는 필수입니다.")
        Boolean overviewRequired,
        @NotNull(message = "포스터 필수 여부는 필수입니다.")
        Boolean posterRequired,
        @NotNull(message = "이미지 필수 여부는 필수입니다.")
        Boolean imagesRequired
) {
}
