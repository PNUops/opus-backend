package com.opus.opus.modules.contest.application.dto.response;

public record AdmissionYearResponse(

        Integer admissionYear,
        Long voterCount,
        Double ratio
) {
}
