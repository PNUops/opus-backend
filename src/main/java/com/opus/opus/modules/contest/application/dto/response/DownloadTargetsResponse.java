package com.opus.opus.modules.contest.application.dto.response;

import java.util.List;

public record DownloadTargetsResponse(

        List<DownloadTargetResponse> targets
) {
}
