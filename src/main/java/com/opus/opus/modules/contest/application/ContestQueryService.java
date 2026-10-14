package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSortConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTemplateConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.response.AdmissionYearResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestTemplateResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteReportResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.DailyVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.HourlyVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamDashboardSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.TrackRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.TrackVoteResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.application.dto.response.VoteUsageResponse;
import com.opus.opus.modules.contest.application.dto.response.VoterRoleResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTemplate;
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
import com.opus.opus.modules.member.domain.MemberRoleType;
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
import com.opus.opus.modules.team.domain.dao.VoteRecordResult;
import com.opus.opus.modules.team.domain.dao.VoteStatisticsResult;
import com.opus.opus.modules.team.domain.dao.projection.TeamAwardProjection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;
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

    private static final int TRACK_TOP_RANK = 3;

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
        final double average = calculateAverage(result.totalVotes(), result.totalVoters());
        return new ContestVoteStatisticsResponse(result.totalVotes(), result.totalVoters(), average);
    }

    public ContestVoteReportResponse getVoteReport(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final List<VoteRecordResult> voteRecords = teamVoteConvenience.getVoteRecords(contest.getId());
        final List<TeamRankingResult> rankingResults = teamConvenience.getTeamRankingResults(contest.getId());
        final List<ContestRankingResponse> rankings = applyRanking(rankingResults);
        final Map<String, List<TeamRankingResult>> rankingResultsByTrack = groupRankingResultsByTrack(rankingResults);

        final Map<Long, Long> voteCountByVoter = voteRecords.stream()
                .collect(groupingBy(VoteRecordResult::memberId, counting()));
        final Collection<Member> voters = memberConvenience.getMembersByIds(new ArrayList<>(voteCountByVoter.keySet()))
                .values();
        final long totalVotes = voteRecords.size();
        final long totalVoters = voteCountByVoter.size();
        final int zeroVoteTeamCount = (int) rankings.stream()
                .filter(ranking -> ranking.voteCount() == 0)
                .count();
        final boolean isVoteEnded = contest.isVoteEnded();
        // 집계 기준 시각: 마감 후에는 투표 종료 시각, 마감 전에는 현재 시각
        final LocalDateTime aggregatedAt = isVoteEnded ? contest.getVoteEndAt() : LocalDateTime.now();

        return new ContestVoteReportResponse(
                contest.getContestName(),
                contest.getVoteStartAt(),
                contest.getVoteEndAt(),
                isVoteEnded,
                aggregatedAt,
                contest.getMaxVotesLimit(),
                totalVoters,
                totalVotes,
                calculateAverage(totalVotes, totalVoters),
                rankings.size(),
                calculateAverage(totalVotes, rankings.size()),
                zeroVoteTeamCount,
                aggregateDailyVotes(voteRecords),
                aggregateVoteUsages(voteCountByVoter, contest.getMaxVotesLimit()),
                aggregateHourlyVotes(voteRecords, contest.getVoteStartAt(), aggregatedAt),
                aggregateVoterRoles(voters, totalVoters),
                aggregateAdmissionYears(voters, totalVoters),
                aggregateTrackVotes(rankingResultsByTrack),
                aggregateTrackTopRankings(rankingResultsByTrack),
                rankings
        );
    }

    private List<DailyVoteResponse> aggregateDailyVotes(final List<VoteRecordResult> voteRecords) {
        return voteRecords.stream()
                .collect(groupingBy(voteRecord -> voteRecord.votedAt().toLocalDate(), TreeMap::new, toList()))
                .entrySet().stream()
                .map(entry -> new DailyVoteResponse(
                        entry.getKey(),
                        entry.getValue().stream().map(VoteRecordResult::memberId).distinct().count(),
                        (long) entry.getValue().size()
                ))
                .toList();
    }

    // 1표 ~ 최대 투표 수(한도 변경으로 한도를 넘긴 투표자가 있으면 그 수까지)별 투표자 수
    private List<VoteUsageResponse> aggregateVoteUsages(final Map<Long, Long> voteCountByVoter,
                                                        final int maxVotesLimit) {
        final Map<Long, Long> voterCountByUsedVotes = voteCountByVoter.values().stream()
                .collect(groupingBy(Function.identity(), counting()));
        final long maxUsedVotes = Math.max(maxVotesLimit,
                voterCountByUsedVotes.keySet().stream().max(Long::compare).orElse(0L));

        return LongStream.rangeClosed(1, maxUsedVotes)
                .mapToObj(usedVotes -> {
                    final long voterCount = voterCountByUsedVotes.getOrDefault(usedVotes, 0L);
                    return new VoteUsageResponse(usedVotes, voterCount,
                            calculateRatio(voterCount, voteCountByVoter.size()));
                })
                .toList();
    }

    // 투표 시작 ~ 집계 기준 시각까지 1시간 단위 투표 수와 누적 투표자 수
    // 투표 기간이 나중에 변경되어 기간 밖에 남은 표가 있으면 그 시각까지 범위를 넓힌다.
    private List<HourlyVoteResponse> aggregateHourlyVotes(final List<VoteRecordResult> voteRecords,
                                                          final LocalDateTime voteStartAt,
                                                          final LocalDateTime aggregatedAt) {
        final Map<LocalDateTime, Long> voteCountByHour = voteRecords.stream()
                .collect(groupingBy(voteRecord -> voteRecord.votedAt().truncatedTo(HOURS), counting()));
        final Map<LocalDateTime, Long> newVoterCountByHour = voteRecords.stream()
                .collect(toMap(VoteRecordResult::memberId, VoteRecordResult::votedAt,
                        BinaryOperator.minBy(Comparator.naturalOrder())))
                .values().stream()
                .collect(groupingBy(firstVotedAt -> firstVotedAt.truncatedTo(HOURS), counting()));

        final LocalDateTime startHour = Stream.concat(Stream.of(voteStartAt),
                        voteCountByHour.keySet().stream())
                .min(Comparator.naturalOrder())
                .orElseThrow()
                .truncatedTo(HOURS);
        final LocalDateTime endHour = Stream.concat(Stream.of(aggregatedAt),
                        voteCountByHour.keySet().stream())
                .max(Comparator.naturalOrder())
                .orElseThrow()
                .truncatedTo(HOURS);

        final List<HourlyVoteResponse> responses = new ArrayList<>();
        long cumulativeVoterCount = 0;
        for (LocalDateTime hour = startHour; !hour.isAfter(endHour); hour = hour.plusHours(1)) {
            cumulativeVoterCount += newVoterCountByHour.getOrDefault(hour, 0L);
            responses.add(new HourlyVoteResponse(hour, voteCountByHour.getOrDefault(hour, 0L),
                    cumulativeVoterCount));
        }
        return responses;
    }

    // 탈퇴 회원은 역할 정보가 함께 삭제되어 대표 역할을 알 수 없다.
    // 탈퇴 회원의 표도 순위·합계에 포함되므로, 역할을 알 수 없는 투표자는 role null로 따로 세어 합계를 맞춘다.
    private List<VoterRoleResponse> aggregateVoterRoles(final Collection<Member> voters, final long totalVoters) {
        final Map<MemberRoleType, Long> voterCountByRole = voters.stream()
                .map(Member::getPrimaryRole)
                .filter(Objects::nonNull)
                .collect(groupingBy(Function.identity(), () -> new EnumMap<>(MemberRoleType.class), counting()));

        final List<VoterRoleResponse> responses = new ArrayList<>(voterCountByRole.entrySet().stream()
                .map(entry -> new VoterRoleResponse(entry.getKey().name(), entry.getValue(),
                        calculateRatio(entry.getValue(), totalVoters)))
                .toList());
        final long unknownRoleVoterCount = totalVoters - sumCounts(voterCountByRole);
        if (unknownRoleVoterCount > 0) {
            responses.add(new VoterRoleResponse(null, unknownRoleVoterCount,
                    calculateRatio(unknownRoleVoterCount, totalVoters)));
        }
        return responses;
    }

    // 학번이 없는 투표자(교수·직원 등)와 탈퇴한 투표자는 admissionYear null로 따로 센다.
    private List<AdmissionYearResponse> aggregateAdmissionYears(final Collection<Member> voters,
                                                                final long totalVoters) {
        final Map<Integer, Long> voterCountByAdmissionYear = voters.stream()
                .map(Member::extractAdmissionYear)
                .filter(Objects::nonNull)
                .collect(groupingBy(Function.identity(), TreeMap::new, counting()));

        final List<AdmissionYearResponse> responses = new ArrayList<>(voterCountByAdmissionYear.entrySet().stream()
                .map(entry -> new AdmissionYearResponse(entry.getKey(), entry.getValue(),
                        calculateRatio(entry.getValue(), totalVoters)))
                .toList());
        final long unknownAdmissionYearVoterCount = totalVoters - sumCounts(voterCountByAdmissionYear);
        if (unknownAdmissionYearVoterCount > 0) {
            responses.add(new AdmissionYearResponse(null, unknownAdmissionYearVoterCount,
                    calculateRatio(unknownAdmissionYearVoterCount, totalVoters)));
        }
        return responses;
    }

    private long sumCounts(final Map<?, Long> countMap) {
        return countMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    // 분과가 없는 팀(trackName null)도 하나의 묶음으로 집계하기 위해 null 키를 허용하는 LinkedHashMap을 쓴다.
    // 분과 순서는 분과 득표 수 내림차순 (같으면 순위가 높은 팀이 속한 분과 먼저)
    private Map<String, List<TeamRankingResult>> groupRankingResultsByTrack(
            final List<TeamRankingResult> rankingResults) {
        final Map<String, List<TeamRankingResult>> rankingResultsByTrack = new LinkedHashMap<>();
        rankingResults.forEach(result ->
                rankingResultsByTrack.computeIfAbsent(result.trackName(), trackName -> new ArrayList<>()).add(result));

        return rankingResultsByTrack.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<String, List<TeamRankingResult>> entry) ->
                        sumVoteCounts(entry.getValue())).reversed())
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (existing, replacement) -> existing, LinkedHashMap::new));
    }

    private List<TrackVoteResponse> aggregateTrackVotes(
            final Map<String, List<TeamRankingResult>> rankingResultsByTrack) {
        return rankingResultsByTrack.entrySet().stream()
                .map(entry -> {
                    final int teamCount = entry.getValue().size();
                    final long voteCount = sumVoteCounts(entry.getValue());
                    return new TrackVoteResponse(entry.getKey(), teamCount, voteCount,
                            calculateAverage(voteCount, teamCount));
                })
                .toList();
    }

    // 분과 안에서 순위를 다시 매겨(동점은 같은 순위) TRACK_TOP_RANK위까지의 팀을 담는다. 득표가 없는 팀은 제외한다.
    private List<TrackRankingResponse> aggregateTrackTopRankings(
            final Map<String, List<TeamRankingResult>> rankingResultsByTrack) {
        return rankingResultsByTrack.entrySet().stream()
                .map(entry -> new TrackRankingResponse(entry.getKey(), applyRanking(entry.getValue()).stream()
                        .filter(ranking -> ranking.rank() <= TRACK_TOP_RANK && ranking.voteCount() > 0)
                        .toList()))
                .toList();
    }

    private long sumVoteCounts(final List<TeamRankingResult> rankingResults) {
        return rankingResults.stream()
                .mapToLong(TeamRankingResult::voteCount)
                .sum();
    }

    private double calculateAverage(final long total, final long count) {
        return count > 0 ? Math.round((double) total / count * 10) / 10.0 : 0.0;
    }

    // 백분율, 소수점 첫째 자리까지
    private double calculateRatio(final long part, final long total) {
        return total > 0 ? Math.round((double) part / total * 1000) / 10.0 : 0.0;
    }

    public List<ContestSubmissionResponse> getTeamSubmissions(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);

        final List<Team> teamList = teamConvenience.getTeamsOfContest(contestId);
        final Map<Long, String> trackNameMap = contestTrackConvenience.getTrackNameMap(contestId);

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
