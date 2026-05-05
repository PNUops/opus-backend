package com.opus.opus.modules.member.application.dto.response;

public record StatisticsSummaryResponse(
        long totalMembers,
        long totalProjects,
        long totalLikes,
        long totalContests
) {}
