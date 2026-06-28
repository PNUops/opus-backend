package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSortConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTemplateConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestTemplateResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamDashboardSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTemplate;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionFeedbackRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.file.application.FileQueryService;
import com.opus.opus.modules.file.application.convenience.FileImageConvenience;
import com.opus.opus.modules.file.application.dto.FileResource;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamContestAwardConvenience;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamLikeConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.application.convenience.TeamVoteConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamRankingResult;
import com.opus.opus.modules.team.domain.dao.VoteStatisticsResult;
import com.opus.opus.modules.team.domain.dao.projection.TeamAwardProjection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestQueryService {

    private final FileQueryService fileQueryService;

    private final ContestRepository contestRepository;
    private final ContestSubmissionItemRepository contestSubmissionItemRepository;
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final ContestSubmissionFeedbackRepository contestSubmissionFeedbackRepository;

    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestSortConvenience contestSortConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestTemplateConvenience contestTemplateConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final TeamVoteConvenience teamVoteConvenience;
    private final TeamLikeConvenience teamLikeConvenience;
    private final MemberConvenience memberConvenience;
    private final TeamContestAwardConvenience teamContestAwardConvenience;
    private final FileImageConvenience fileImageConvenience;

    private static List<ContestRankingResponse> applyRanking(List<TeamRankingResult> votesPerTeam) {
        List<ContestRankingResponse> responseList = new ArrayList<>();

        for (int i = 0; i < votesPerTeam.size(); i++) {
            final TeamRankingResult result = votesPerTeam.get(i);
            final int rank =
                    (i == 0 || !Objects.equals(result.voteCount(), votesPerTeam.get(i - 1).voteCount())) ? i + 1
                            : responseList.get(i - 1).rank();

            responseList.add(new ContestRankingResponse(
                    rank, result.teamId(), result.teamName(), result.projectName(), result.trackName(),
                    result.voteCount()
            ));
        }

        return responseList;
    }

    public ImageResponse getContestBanner(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final FileImage findBanner = fileImageConvenience.findByReferenceIdAndReferenceTypeAndImageType(contestId,
                CONTEST, BANNER);

        checkImageConverted(findBanner);

        final FileResource storageResult = fileQueryService.findFileAndType(findBanner.getFile().getId());
        return new ImageResponse(storageResult.resource(), storageResult.mimeType());
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

    public Page<ContestVoteLogResponse> getContestVoteLog(final Long contestId, final int page, final int size) {
        contestConvenience.validateExistContest(contestId);

        final Pageable pageable = PageRequest.of(page, size, Sort.by(DESC, "createdAt"));

        final Page<TeamVote> votePage = getContestVotes(contestId, pageable);
        final Map<Long, Member> memberMap = getMemberMap(votePage);

        return votePage.map(vote -> {
            final Member member = memberMap.get(vote.getMemberId());
            return new ContestVoteLogResponse(
                    member.getName(),
                    member.getEmail(),
                    vote.getTeam().getTeamName(),
                    vote.getCreatedAt()
            );
        });
    }

    private Page<TeamVote> getContestVotes(final Long contestId, final Pageable pageable) {
        final List<Long> teamIds = teamConvenience.getTeamsOfContest(contestId)
                .stream()
                .map(Team::getId)
                .toList();

        return teamVoteConvenience.getAllTeamVoteDesc(teamIds, pageable);
    }

    private Map<Long, Member> getMemberMap(final Page<TeamVote> votePage) {
        return memberConvenience.getMembersByIds(
                votePage.getContent().stream()
                        .map(TeamVote::getMemberId)
                        .distinct()
                        .toList()
        );
    }

    public MemberVoteCountResponse getMemberVoteCount(Long memberId, Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final long currentVoteCount = teamVoteConvenience.countMemberVotesInContest(memberId, contestId);
        final long remainingVotesCount = contest.getMaxVotesLimit() - currentVoteCount;
        return new MemberVoteCountResponse(remainingVotesCount, (long) contest.getMaxVotesLimit());
    }

    public List<ContestRankingResponse> getTeamRanking(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final List<TeamRankingResult> votesPerTeam = teamConvenience.getTeamRankingResults(contestId);
        return applyRanking(votesPerTeam);
    }

    public ContestVoteStatisticsResponse getVoteStatistics(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final VoteStatisticsResult result = teamVoteConvenience.getVoteStaticsResult(contestId);
        final double average = result.totalVoters() > 0
                ? Math.round((double) result.totalVotes() / result.totalVoters() * 10) / 10.0
                : 0.0;
        return new ContestVoteStatisticsResponse(result.totalVotes(), result.totalVoters(), average);
    }

    public List<ContestSubmissionResponse> getTeamSubmissions(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);

        final List<Team> teamList = teamConvenience.getTeamsOfContest(contestId);
        final Map<Long, String> trackNameMap = contestTrackConvenience.getValidateExistTracks(contestId)
                .stream()
                .collect(Collectors.toMap(ContestTrack::getId, ContestTrack::getTrackName));

        return teamList.stream()
                .map(team -> ContestSubmissionResponse.from(team, trackNameMap.get(team.getTrackId())))
                .toList();
    }

    public List<TeamSummaryResponse> getContestTeamSummaries(final Long contestId, final Member member) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final List<Team> teams = getSortedTeams(contestId, member);

        final VoteLikeResult voteLikeResult = getVoteLikeResult(contestId, member, contest.isVotingPeriod());
        final Map<Long, List<TeamAwardProjection>> teamAwardResultMap = teamContestAwardConvenience.getTeamAwardProjectionMap(
                teams);

        return buildTeamSummaryResponses(teams, teamAwardResultMap, voteLikeResult);
    }

    private List<Team> getSortedTeams(final Long contestId, final Member member) {
        final List<Team> teams = teamConvenience.getTeamsOfContest(contestId);
        final ContestSort contestSort = contestSortConvenience.getValidateExistContestSort(contestId);
        teamConvenience.sortTeams(teams, contestSort.getMode(), member);
        return teams;
    }

    private VoteLikeResult getVoteLikeResult(final Long contestId, final Member member, final boolean isVotingPeriod) {
        final Set<Long> votedTeamIds = teamVoteConvenience.getVotedTeamIdsIfInPeriod(contestId, member,
                isVotingPeriod);
        final Set<Long> likedTeamIds = teamLikeConvenience.getLikedTeamIdsIfInPeriod(contestId, member,
                isVotingPeriod);
        return new VoteLikeResult(votedTeamIds, likedTeamIds);
    }

    private List<TeamSummaryResponse> buildTeamSummaryResponses(final List<Team> teams,
                                                                final Map<Long, List<TeamAwardProjection>> teamAwardResultMap,
                                                                final VoteLikeResult voteLikeResult
    ) {
        return teams.stream()
                .map(team -> TeamSummaryResponse.of(
                        team,
                        teamAwardResultMap.getOrDefault(team.getId(), Collections.emptyList()),
                        voteLikeResult.likedTeamIds().contains(team.getId()),
                        voteLikeResult.votedTeamIds().contains(team.getId())
                ))
                .toList();
    }

    public List<TeamSummaryResponse> getContestTeamSummariesPublic(final Long contestId) {
        return getContestTeamSummaries(contestId, null);
    }

    private void checkImageConverted(final FileImage findFileImage) {
        if (!findFileImage.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }

    public ContestTemplateResponse getContestTemplate(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final ContestTemplate template = contestTemplateConvenience.getValidateExistTemplate(contestId);
        return ContestTemplateResponse.from(template);
    }

    public TeamDashboardSummaryResponse getTeamDashboardSummary(final Long contestId, final Long teamId,
                                                                final Member member) {
        contestConvenience.validateExistContest(contestId);
        final Team team = teamConvenience.getValidateTeamInContest(teamId, contestId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);

        final LocalDateTime now = LocalDateTime.now();
        final List<ContestSubmissionItem> futureItems = team.getTrackId() != null
                ? contestSubmissionItemRepository.findFutureItemsByContestAndTrack(contestId, team.getTrackId(), now)
                : contestSubmissionItemRepository.findFutureCommonItemsByContest(contestId, now);

        final List<ContestSubmissionItem> pendingItems = futureItems.stream()
                .filter(item -> !contestSubmissionRepository.existsByTeamIdAndSubmissionItem(teamId, item))
                .toList();

        final long pendingSubmissionCount = pendingItems.size();
        final LocalDateTime nearestDeadline = pendingItems.stream()
                .map(ContestSubmissionItem::getEndAt)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        final long unreadFeedbackCount =
                contestSubmissionFeedbackRepository.countBySubmission_TeamIdAndIsReadFalse(teamId);
        final String latestFeedbackPreview = contestSubmissionFeedbackRepository
                .findTopBySubmission_TeamIdOrderByCreatedAtDesc(teamId)
                .map(f -> f.getDescription())
                .orElse(null);

        return new TeamDashboardSummaryResponse(pendingSubmissionCount, nearestDeadline,
                unreadFeedbackCount, latestFeedbackPreview);
    }

    private record VoteLikeResult(Set<Long> votedTeamIds, Set<Long> likedTeamIds) {
    }
}
