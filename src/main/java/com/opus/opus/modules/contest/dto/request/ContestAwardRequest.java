package com.opus.opus.modules.contest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ContestAwardRequest {

    @NotBlank(message = "수상명 또는 색상 정보가 올바르지 않습니다.")
    private String awardName;

    @NotBlank(message = "수상명 또는 색상 정보가 올바르지 않습니다.")
    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "색상 정보가 올바르지 않습니다. (예: #FFFFFF)"
    )
    private String awardColor;
}
