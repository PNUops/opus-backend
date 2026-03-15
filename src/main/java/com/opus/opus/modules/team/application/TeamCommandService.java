package com.opus.opus.modules.team.application;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.request.TeamCreateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCreateResponse;
import com.opus.opus.modules.team.application.dto.response.TeamLikeToggleResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamLikeException;
import com.opus.opus.modules.team.exception.TeamVoteException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.opus.opus.modules.file.domain.FileImageType.*;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.EXCEED_PREVIEW_LIMIT;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.*;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamCommandService {

    private final FileStorageUtil fileStorageUtil;

    private final TeamConvenience teamConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestTrackConvenience contestTrackConvenience;

    private final TeamRepository teamRepository;
    private final TeamVoteRepository teamVoteRepository;
    private final TeamLikeRepository teamLikeRepository;
    private final FileRepository fileRepository;


    public TeamCreateResponse createTeam(final TeamCreateRequest request) {
        contestConvenience.validateExistContest(request.contestId());
        if (request.trackId() != null) {
            contestTrackConvenience.getValidateExistTrack(request.contestId(), request.trackId());
        }
        final Team team = teamRepository.save(Team.from(request));
        return TeamCreateResponse.from(team);
    }

    public void deleteTeam(final Long teamId) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        teamRepository.delete(team);
    }

    public void savePreviewImages(final Long teamId, final List<MultipartFile> images) {
        teamConvenience.validateExistTeam(teamId);
        checkPreviewLimit(teamId, images);
        for (MultipartFile image : images) {
            fileStorageUtil.storeFile(image, teamId, TEAM, PREVIEW);
        }
    }

    public void deletePreviewImages(final Long teamId, final List<Long> ids) {
        teamConvenience.validateExistTeam(teamId);
        ids.forEach(fileStorageUtil::deleteFile);
    }

    public void saveThumbnailImage(final Long teamId, final MultipartFile image) {
        teamConvenience.validateExistTeam(teamId);
        final Optional<File> existingFile = fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM,
                THUMBNAIL);
        fileStorageUtil.storeFile(image, teamId, TEAM, THUMBNAIL);
        existingFile.ifPresent(file -> fileStorageUtil.deleteFile(file.getId()));
    }

    public void deleteThumbnailImage(final Long teamId) {
        teamConvenience.validateExistTeam(teamId);
        deleteIfExists(teamId, THUMBNAIL);
    }

    public void savePosterImage(final Long teamId, final MultipartFile image) {
        teamConvenience.validateExistTeam(teamId);
        final Optional<File> existingFile = fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM,
                POSTER);
        fileStorageUtil.storeFile(image, teamId, TEAM, POSTER);
        existingFile.ifPresent(file -> fileStorageUtil.deleteFile(file.getId()));
    }

    public void deletePosterImage(final Long teamId) {
        teamConvenience.validateExistTeam(teamId);
        deleteIfExists(teamId, POSTER);
    }

    public TeamLikeToggleResponse toggleLike(final Long memberId, final Long teamId, final Boolean isLiked) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        final Contest contest = contestConvenience.getValidateExistContest(team.getContestId());

        contestConvenience.validateNotInVotingPeriod(contest);

        final Optional<TeamLike> teamLikeOptional = teamLikeRepository.findByMemberIdAndTeam(memberId, team);
        return teamLikeOptional
                .map(teamLike -> handleExistingLike(teamLike, isLiked))
                .orElseGet(() -> handleFirstTimeLike(memberId, team, isLiked));
    }

    public TeamVoteToggleResponse toggleVote(Long memberId, Long teamId, Boolean isVoted) {
        Team team = teamConvenience.getValidateExistTeam(teamId);
        Contest contest = contestConvenience.getValidateExistContest(team.getContestId());

        contestConvenience.validateVotingPeriod(contest);

        Optional<TeamVote> teamVoteOptional = teamVoteRepository.findByMemberIdAndTeam(memberId, team);

        return teamVoteOptional.map(teamVote -> handleExistingVote(teamVote, isVoted, memberId, contest))
                .orElseGet(() -> handleFirstTimeVote(memberId, team, isVoted, contest));
    }


    private TeamLikeToggleResponse handleFirstTimeLike(final Long memberId, final Team team, final Boolean isLiked) {
        if (!isLiked) {
            throw new TeamLikeException(NOT_LIKED_YET);
        }

        saveTeamLike(memberId, team);
        return TeamLikeToggleResponse.of(team.getId(), true, "좋아요가 등록되었습니다.");
    }

    private TeamLikeToggleResponse handleExistingLike(final TeamLike teamLike, final Boolean isLiked) {
        if (Objects.equals(teamLike.getIsLiked(), isLiked)) {
            throw new TeamLikeException(isLiked ? ALREADY_LIKED : ALREADY_UNLIKED);
        }

        teamLike.updateIsLiked(isLiked);

        return TeamLikeToggleResponse.of(teamLike.getTeam().getId(), isLiked,
                isLiked ? "좋아요가 등록되었습니다." : "좋아요가 취소되었습니다.");
    }

    private void saveTeamLike(final Long memberId, final Team team) {
        teamLikeRepository.save(TeamLike.builder()
                .memberId(memberId)
                .team(team)
                .isLiked(true)
                .build());
    }

    private TeamVoteToggleResponse handleFirstTimeVote(Long memberId, Team team, Boolean isVoted, Contest contest) {
        if (!isVoted) {
            throw new TeamVoteException(NOT_VOTED_YET);
        }

        long currentVoteCount = countCurrentMemberVotes(memberId, team.getContestId());
        int maxVotesLimit = contest.getMaxVotesLimit();

        validateVoteLimit(currentVoteCount, maxVotesLimit);
        saveTeamVote(memberId, team, true);

        return TeamVoteToggleResponse.of(team.getId(), true, "투표가 등록되었습니다.", currentVoteCount + 1, maxVotesLimit);
    }

    private TeamVoteToggleResponse handleExistingVote(final TeamVote teamVote, final Boolean isVoted,
                                                      final Long memberId, final Contest contest) {
        if (Objects.equals(teamVote.getIsVoted(), isVoted)) {
            throw new TeamVoteException(isVoted ? ALREADY_VOTED : ALREADY_UNVOTED);
        }

        final long currentVoteCount = countCurrentMemberVotes(memberId, contest.getId());
        final int maxVotesLimit = contest.getMaxVotesLimit();

        if (isVoted) {
            validateVoteLimit(currentVoteCount, maxVotesLimit);
        }

        final long updatedVoteCount = currentVoteCount + (isVoted ? 1 : -1);
        teamVote.updateIsVoted(isVoted);

        return TeamVoteToggleResponse.of(teamVote.getTeam().getId(), isVoted, isVoted ? "투표가 등록되었습니다." : "투표가 취소되었습니다.",
                updatedVoteCount, maxVotesLimit);
    }

    private long countCurrentMemberVotes(Long memberId, Long contestId) {
        return teamVoteRepository.countMemberVotesInContest(memberId, contestId);
    }

    private void validateVoteLimit(long currentVoteCount, int maxVotesLimit) {
        if (currentVoteCount >= maxVotesLimit) {
            String message = String.format(VOTE_LIMIT_EXCEEDED.errorMessage(), maxVotesLimit);
            throw new TeamVoteException(VOTE_LIMIT_EXCEEDED, message);
        }
    }

    private void saveTeamVote(Long memberId, Team team, Boolean isVoted) {
        try {
            teamVoteRepository.save(TeamVote.builder()
                    .memberId(memberId)
                    .team(team)
                    .isVoted(isVoted)
                    .build());
            teamVoteRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new TeamVoteException(DUPLICATE_VOTE_REQUEST);
        }
    }

    private void deleteIfExists(final Long teamId, final FileImageType imageType) {
        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, imageType)
                .ifPresent(existingFile -> fileStorageUtil.deleteFile(existingFile.getId()));
    }

    private void checkPreviewLimit(final Long teamId, final List<MultipartFile> images) {
        long savedCount = fileRepository.countByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, PREVIEW);
        if (savedCount + images.size() > 5) {
            throw new FileException(EXCEED_PREVIEW_LIMIT);
        }
    }
}
