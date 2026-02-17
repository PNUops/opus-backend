package com.opus.opus.modules.contest.application.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * 필수 항목 설정 요청
 * 각 항목은 필수 여부만 설정 가능합니다. (true: 필수 입력, false: 선택 입력)
 */
public record TeamTemplateRequest(
        @NotNull(message = "분과 필수 여부는 필수입니다.")
        Boolean divisionRequired,
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
        Boolean youtubePathRequired,
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

