package com.opus.opus.modules.member.application.dto.request;

import jakarta.validation.constraints.Pattern;

public record GithubUrlUpdateRequest(

        @Pattern(regexp = "^(https://github\\.com/.+)?$", message = "올바른 GitHub URL을 입력해주세요.")
        String githubUrl
) {
}
