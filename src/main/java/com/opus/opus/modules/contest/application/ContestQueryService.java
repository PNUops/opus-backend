package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;
import static org.springframework.data.domain.Sort.Direction.DESC;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSortConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamVoteConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamVote;
import java.util.List;
import java.util.Map;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.domain.dao.TeamRankingResult;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.domain.dao.VoteStatisticsResult;
import java.util.ArrayList;

import java.util.Objects;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
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

    private final FileStorageUtil fileStorageUtil;

    private final ContestRepository contestRepository;
    private final TeamRepository teamRepository;
    private final TeamVoteRepository teamVoteRepository;
    private final ContestTrackRepository contestTrackRepository;

    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestSortConvenience contestSortConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamVoteConvenience teamVoteConvenience;
    private final MemberConvenience memberConvenience;
    private final FileConvenience fileConvenience;

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
        final long currentVoteCount = teamVoteRepository.countMemberVotesInContest(memberId, contestId);
        final long remainingVotesCount = contest.getMaxVotesLimit() - currentVoteCount;
        return new MemberVoteCountResponse(remainingVotesCount, (long) contest.getMaxVotesLimit());
    }

    public List<ContestRankingResponse> getTeamRanking(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final List<TeamRankingResult> votesPerTeam = teamRepository.findTeamRankingByContestId(contestId);
        return applyRanking(votesPerTeam);
    }

    public ContestVoteStatisticsResponse getVoteStatistics(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final VoteStatisticsResult result = teamVoteRepository.countVoteStatisticsByContest(contestId);
        final double average = result.totalVoters() > 0
                ? Math.round((double) result.totalVotes() / result.totalVoters() * 10) / 10.0
                : 0.0;
        return new ContestVoteStatisticsResponse(result.totalVotes(), result.totalVoters(), average);
    }

    public List<ContestSubmissionResponse> getTeamSubmissions(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);

        final List<Team> teamList = teamRepository.findAllByContestId(contestId);
        final Map<Long, String> trackNameMap = contestTrackRepository.findAllByContestId(contestId)
                .stream()
                .collect(Collectors.toMap(ContestTrack::getId, ContestTrack::getTrackName));

        return teamList.stream()
                .map(team -> ContestSubmissionResponse.from(team, trackNameMap.get(team.getTrackId())))
                .toList();
    }

    private static List<ContestRankingResponse> applyRanking(List<TeamRankingResult> votesPerTeam) {
        List<ContestRankingResponse> responseList = new ArrayList<>();

        for (int i = 0; i < votesPerTeam.size(); i++) {
            final TeamRankingResult result = votesPerTeam.get(i);
            final int rank = (i == 0 || !Objects.equals(result.voteCount(), votesPerTeam.get(i - 1).voteCount())) ? i + 1 : responseList.get(i - 1).rank();

            responseList.add(new ContestRankingResponse(
                    rank, result.teamId(), result.teamName(), result.projectName(), result.trackName(), result.voteCount()
            ));
        }

        return responseList;
    }

    private void checkImageConverted(final File findFile) {
        if (!findFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }

}
