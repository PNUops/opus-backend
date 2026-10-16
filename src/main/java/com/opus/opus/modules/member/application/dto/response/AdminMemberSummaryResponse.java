package com.opus.opus.modules.member.application.dto.response;

public record AdminMemberSummaryResponse(

        long activeCount,

        long withdrawnCount
) {
}
