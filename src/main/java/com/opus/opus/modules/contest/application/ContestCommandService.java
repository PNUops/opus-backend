package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.contest.domain.SortType.CUSTOM;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ALREADY_CURRENT_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ALREADY_NOT_CURRENT_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CURRENT_CONTEST_LIMIT_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_TEAM_ID_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_CONTEST_SORT_CUSTOM_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_EXIST_TEAM_IN_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.VOTE_END_PRECEDE_VOTE_START;
import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;
import static com.opus.opus.modules.team.exception.TeamExceptionType.INVALID_ITEM_ORDER;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSortConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentToggleResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestCommandService {

    private static final int MAX_CURRENT_CONTEST_COUNT = 2;

    private final ContestRepository contestRepository;
    private final FileRepository fileRepository;

    private final ContestConvenience contestConvenience;
    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestSortConvenience contestSortConvenience;
    private final TeamConvenience teamConvenience;

    private final FileStorageUtil fileStorageUtil;


    public void saveBannerImage(final Long contestId, final MultipartFile image) {
        contestConvenience.getValidateExistContest(contestId);

        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(contestId, CONTEST, BANNER).ifPresent(file -> {
            checkWebpConverted(file);
            fileStorageUtil.deleteFile(file.getId());
        });
        fileStorageUtil.storeFile(image, contestId, CONTEST, BANNER);
    }

    public void deleteBannerImage(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);

        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(contestId, CONTEST, BANNER).ifPresent(file ->
                fileStorageUtil.deleteFile(file.getId())
        );
    }

    public ContestResponse createContest(final ContestRequest request) {
        contestConvenience.validateDuplicateContestName(request.contestName());
        ContestCategory contestCategory = contestCategoryConvenience.getValidateExistCategory(request.categoryId());
        final Contest contest = Contest.builder()
                .contestName(request.contestName())
                .categoryId(request.categoryId())
                .build();
        contestRepository.save(contest);

        return ContestResponse.from(contest, contestCategory.getCategoryName());
    }

    public void updateContest(final Long contestId, final ContestRequest request) {
        contestConvenience.validateDuplicateContestName(request.contestName());
        contestCategoryConvenience.getValidateExistCategory(request.categoryId());
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        contest.updateContest(request.categoryId(), request.contestName());
    }

    public void deleteContest(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        teamConvenience.validateAllTeamsDeletedInContest(contestId);
        contestRepository.delete(contest);
    }

    public ContestCurrentToggleResponse toggleCurrent(final Long contestId, final Boolean isCurrent) {
        Contest contest = contestConvenience.getValidateExistContest(contestId);
        // 기존과 동일한 값 요청이면 예외
        validateSameCurrentRequest(contest.getIsCurrent(), isCurrent);
        // 현재 대회로 등록하는 경우, 현재 진행 중인 대회 최대 개수 검사
        if (isCurrent) {
            long currentCount = contestConvenience.countCurrentContests();
            validateCurrentContestLimit(currentCount);
        }
        // 변경
        contest.updateIsCurrent(isCurrent);
        return ContestCurrentToggleResponse.of(contest.getId(), isCurrent);
    }

    public void updateVotePeriod(final Long contestId, final VoteUpdateRequest voteRequest) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        checkVoteRange(voteRequest);
        contest.updateVotePeriod(voteRequest.voteStartAt(), voteRequest.voteEndAt());
    }

    private void checkVoteRange(final VoteUpdateRequest voteRequest) {
        final int compare = voteRequest.voteStartAt().compareTo(voteRequest.voteEndAt());
        if (compare > 0) {
            throw new ContestException(VOTE_END_PRECEDE_VOTE_START);
        }
    }

    public void updateMaxVotesLimit(final Long contestId, final Integer maxVotesLimit) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);

        validateNotInVotingPeriod(contest);

        contest.updateMaxVotesLimit(maxVotesLimit);
    }

    public void updateContestSort(final Long contestId, final ContestSortRequest request) {
        contestConvenience.validateExistContest(contestId);
        final ContestSort contestSort = contestSortConvenience.getValidateExistContestSort(contestId);

        contestSort.updateMode(request.mode());
    }

    //todo: 팀 생성 시 itemOrder 추가
    public void updateContestSortCustom(final Long contestId, final List<ContestSortCustomRequest> requests) {
        final Contest contest = contestConvenience.getValidateExistContestForUpdate(contestId);
        final ContestSort contestSort = contestSortConvenience.getValidateExistContestSort(contest.getId());
        checkCustomSort(contestSort);

        validateDuplicateTeamIds(requests);
        validateDuplicateItemOrders(requests);

        final List<Team> teams = teamConvenience.getTeamsOfContest(contestId);
        validateRequestSizeMatchesTeams(requests, teams);
        validateItemOrderRange(requests, teams.size());

        applyCustomSortToTeams(requests, teams);
    }

    private void validateNotInVotingPeriod(final Contest contest) {
        if (contest.isVotingPeriod()) {
            throw new ContestException(CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD);
        }
    }

    private void checkWebpConverted(File existingFile) {
        if (!existingFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }

    private void validateSameCurrentRequest(final Boolean currentValue, final Boolean requestValue) {
        if (currentValue == requestValue) {
            throw new ContestException(currentValue ? ALREADY_CURRENT_CONTEST : ALREADY_NOT_CURRENT_CONTEST);
        }
    }

    private void validateCurrentContestLimit(final long currentCount) {
        if (currentCount >= MAX_CURRENT_CONTEST_COUNT) {
            throw new ContestException(CURRENT_CONTEST_LIMIT_EXCEEDED);
        }
    }

    private void validateItemOrderRange(final List<ContestSortCustomRequest> requests, final int teamCount) {
        for (ContestSortCustomRequest r : requests) {
            final int order = r.itemOrder();
            if (order < 1 || order > teamCount) {
                throw new TeamException(INVALID_ITEM_ORDER);
            }
        }
    }

    private void checkCustomSort(final ContestSort contestSort) {
        if (contestSort.getMode() != CUSTOM) {
            throw new ContestException(ONLY_CUSTOM_MODE_CAN_CHANGE);
        }
    }

    private void validateRequestSizeMatchesTeams(final List<ContestSortCustomRequest> requests,
                                                 final List<Team> teams) {
        if (requests.size() != teams.size()) {
            throw new ContestException(INVALID_CONTEST_SORT_CUSTOM_REQUEST);
        }
    }

    private void validateDuplicateTeamIds(final List<ContestSortCustomRequest> requests) {
        if (requests.stream().map(ContestSortCustomRequest::teamId).distinct().count() != requests.size()) {
            throw new ContestException(DUPLICATE_TEAM_ID_IN_SORT_REQUEST);
        }
    }

    private void validateDuplicateItemOrders(final List<ContestSortCustomRequest> requests) {
        if (requests.stream().map(ContestSortCustomRequest::itemOrder).distinct().count() != requests.size()) {
            throw new ContestException(DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST);
        }
    }

    private void applyCustomSortToTeams(final List<ContestSortCustomRequest> requests, final List<Team> teams) {
        final Map<Long, Team> teamMap = teams.stream()
                .collect(toMap(Team::getId, identity()));

        for (ContestSortCustomRequest r : requests) {
            final Team team = teamMap.get(r.teamId());
            if (team == null) {
                throw new ContestException(NOT_EXIST_TEAM_IN_CONTEST);
            }
            team.updateItemOrder(r.itemOrder());
        }
    }
}
