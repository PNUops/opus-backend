package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSortConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamLikeConvenience;
import com.opus.opus.modules.team.application.convenience.TeamVoteConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.domain.Team;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestQueryService {

    private final FileStorageUtil fileStorageUtil;

    private final ContestRepository contestRepository;

    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestSortConvenience contestSortConvenience;
    private final FileConvenience fileConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamLikeConvenience teamLikeConvenience;
    private final TeamVoteConvenience teamVoteConvenience;

    public ImageResponse getContestBanner(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final File findBanner = fileConvenience.findByReferenceIdAndReferenceTypeAndImageType(contestId, CONTEST,
                BANNER);

        checkImageConverted(findBanner);

        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findBanner.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    public List<ContestCurrentResponse> getCurrentContests() {
        List<Contest> contests = contestConvenience.getCurrentContests();

        return contests.stream()
                .map(contest -> {
                    String categoryName = contestCategoryConvenience.getValidateExistCategory(contest.getCategoryId())
                            .getCategoryName();
                    return ContestCurrentResponse.of(contest, categoryName);
                })
                .toList();
    }

    public List<ContestResponse> getAllContests() {
        List<Contest> contests = contestRepository.findAll();

        return contests.stream()
                .map(contest -> {
                    ContestCategory category = contestCategoryConvenience.getValidateExistCategory(
                            contest.getCategoryId());
                    return ContestResponse.from(contest, category.getCategoryName());
                })
                .toList();
    }

    public VotePeriodResponse getVotePeriod(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        return new VotePeriodResponse(contest.getVoteStartAt(), contest.getVoteEndAt());
    }

    public ContestVotesLimitResponse getMaxVotesLimit(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        return ContestVotesLimitResponse.from(contest.getMaxVotesLimit());
    }

    public ContestSortResponse getContestSort(final Long contestId) {
        contestConvenience.validateExistContest(contestId);
        final ContestSort contestSort = contestSortConvenience.getValidateExistContestSort(contestId);

        return new ContestSortResponse(contestSort.getMode());
    }

    public List<TeamSummaryResponse> getContestTeamSummaries(final Long contestId, final Member member) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final List<Team> teams = teamConvenience.findAllByContestId(contestId);

        final boolean isVotingPeriod = checkVotingPeriod(contest);

        final Pair<Map<Long, Boolean>, Map<Long, Boolean>> voteAndLikeMaps = getVoteAndLikeMaps(teams, member,
                isVotingPeriod);
        final Map<Long, Boolean> voteMap = voteAndLikeMaps.a;
        final Map<Long, Boolean> likeMap = voteAndLikeMaps.b;

        final List<ContestAward> teamAwards = contestAwardConvenience.getTeamAwards(teams);

        teamConvenience.shuffleTeams(teams, member);

        return teams.stream()
                .map(team -> TeamSummaryResponse.of(team, teamAwards,
                        likeMap.getOrDefault(team.getId(), false),
                        voteMap.getOrDefault(team.getId(), false),
                        isVotingPeriod
                ))
                .toList();
    }

    public List<TeamSummaryResponse> getContestTeamSummariesPublic(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final List<Team> teams = teamConvenience.findAllByContestId(contestId);

        final boolean isVotingPeriod = checkVotingPeriod(contest);

        final List<ContestAward> teamAwards = contestAwardConvenience.getTeamAwards(teams);

        teamConvenience.shuffleTeams(teams);

        return teams.stream()
                .map(team -> TeamSummaryResponse.of(team, teamAwards,
                        false,
                        false,
                        isVotingPeriod
                ))
                .toList();
    }

    private Pair<Map<Long, Boolean>, Map<Long, Boolean>> getVoteAndLikeMaps(
            final List<Team> teams, final Member member, final boolean isVotingPeriod) {
        if (isVotingPeriod) {
            return new Pair<>(
                    teamVoteConvenience.getVoteMap(teams, member),
                    Collections.emptyMap()
            );
        } else {
            return new Pair<>(
                    Collections.emptyMap(),
                    teamLikeConvenience.getLikeMap(teams, member)
            );
        }
    }

    private boolean checkVotingPeriod(final Contest contest) {
        final LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(contest.getVoteStartAt())
                && !now.isAfter(contest.getVoteEndAt());
    }

    private void checkImageConverted(final File findFile) {
        if (!findFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
