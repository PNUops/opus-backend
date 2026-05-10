package com.opus.opus.modules.member.application;

import com.opus.opus.global.util.CacheRedisUtil;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.response.StatisticsSummaryResponse;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamLikeConvenience;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsQueryService {

    private static final String STATISTICS_CACHE_KEY = "statistics:summary";
    private static final long STATISTICS_CACHE_TTL = 10L;

    private final MemberConvenience memberConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamLikeConvenience teamLikeConvenience;
    private final ContestConvenience contestConvenience;
    private final CacheRedisUtil cacheRedisUtil;

    public StatisticsSummaryResponse getStatisticsSummary() {
        try {
            final Object cached = cacheRedisUtil.get(STATISTICS_CACHE_KEY);
            if (cached instanceof StatisticsSummaryResponse response) {
                return response;
            }
        } catch (final Exception e) {
            log.warn("Redis 캐시 조회 실패, DB에서 직접 조회합니다.", e);
        }

        final StatisticsSummaryResponse response = queryStatisticsFromDb();

        try {
            cacheRedisUtil.set(STATISTICS_CACHE_KEY, response, STATISTICS_CACHE_TTL, TimeUnit.MINUTES);
        } catch (final Exception e) {
            log.warn("Redis 캐시 저장 실패", e);
        }

        return response;
    }

    private StatisticsSummaryResponse queryStatisticsFromDb() {
        final long totalMembers = memberConvenience.countAllMembers();
        final long totalProjects = teamConvenience.countSubmittedTeams();
        final long totalLikes = teamLikeConvenience.countAllLikes();
        final long totalContests = contestConvenience.countAllContests();
        return new StatisticsSummaryResponse(totalMembers, totalProjects, totalLikes, totalContests);
    }
}
