package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.PREVIEW;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.EXCEED_PREVIEW_LIMIT;
import static com.opus.opus.modules.team.exception.TeamExceptionType.FORBIDDEN_CONTEST_OR_TRACK_UPDATE;
import static com.opus.opus.modules.team.exception.TeamExceptionType.REQUIRED_FIELD_MISSING;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_LIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.ALREADY_UNLIKED;
import static com.opus.opus.modules.team.exception.TeamLikeExceptionType.NOT_LIKED_YET;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_UNVOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.ALREADY_VOTED;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.DUPLICATE_VOTE_REQUEST;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.NOT_VOTED_YET;
import static com.opus.opus.modules.team.exception.TeamVoteExceptionType.VOTE_LIMIT_EXCEEDED;

import com.opus.opus.modules.file.application.FileCommandService;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTemplateConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTemplate;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.application.dto.request.TeamCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamUpdateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCreateResponse;
import com.opus.opus.modules.team.application.dto.response.TeamLikeToggleResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamLike;
import com.opus.opus.modules.team.domain.TeamVote;
import com.opus.opus.modules.team.domain.dao.TeamContestAwardRepository;
import com.opus.opus.modules.team.domain.dao.TeamLikeRepository;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import com.opus.opus.modules.team.exception.TeamException;
import com.opus.opus.modules.team.exception.TeamLikeException;
import com.opus.opus.modules.team.exception.TeamVoteException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamCommandService {

    private final FileCommandService fileCommandService;

    private final TeamRepository teamRepository;
    private final TeamVoteRepository teamVoteRepository;
    private final TeamLikeRepository teamLikeRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamContestAwardRepository teamContestAwardRepository;
    private final FileRepository fileRepository;

    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestTemplateConvenience contestTemplateConvenience;
    private final FileConvenience fileConvenience;


    public TeamCreateResponse createTeam(final TeamCreateRequest request) {
        contestConvenience.validateExistContest(request.contestId());
        if (request.trackId() != null) {
            contestTrackConvenience.getValidateExistTrack(request.contestId(), request.trackId());
        }

        // 해당 대회의 최대 itemOrder + 1로 순서 자동 부여
        int nextOrder = teamRepository.findMaxItemOrderByContestId(request.contestId()) + 1;

        final Team team = teamRepository.save(Team.from(request, nextOrder));
        return TeamCreateResponse.from(team);
    }

    public void deleteTeam(final Long teamId) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        deleteTeamImages(teamId);
        reorderAfterTeamDeletion(team);
        teamMemberRepository.deleteAll(team.getTeamMembers());
        teamVoteRepository.deleteAll(team.getTeamVotes());
        teamLikeRepository.deleteAll(team.getTeamLikes());
        teamContestAwardRepository.deleteAll(team.getTeamAwards());
        teamRepository.delete(team);
    }

    private void reorderAfterTeamDeletion(final Team team) {
        final Long contestId = team.getContestId();
        final int deletedOrder = team.getItemOrder();

        teamRepository.updateItemOrderAfterDeletion(contestId, deletedOrder);
    }

    private void deleteTeamImages(final Long teamId) {
        deleteIfExists(teamId, POSTER);
        deleteIfExists(teamId, THUMBNAIL);
        final List<Long> previewIds = fileConvenience.findAllPreviewIdsByTeamId(teamId);
        previewIds.forEach(fileCommandService::deleteFile);
    }

    public void updateTeam(final Member member, final Long teamId, final TeamUpdateRequest request) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);

        if (member.isAdmin()) {
            updateTeamByAdmin(team, request);
        } else {
            updateTeamByMember(member.getId(), team, request);
        }
    }

    private void updateTeamByAdmin(final Team team, final TeamUpdateRequest request) {
        validateContestIfChanged(team, request);
        validateTrackInContestIfChanged(team, request);
        team.update(request);
    }

    private void validateContestIfChanged(final Team team, final TeamUpdateRequest request) {
        if (request.contestId() != null && !request.contestId().equals(team.getContestId())) {
            contestConvenience.validateExistContest(request.contestId());
        }
    }

    private void validateTrackInContestIfChanged(final Team team, final TeamUpdateRequest request) {
        final Long contestIdToValidate = request.contestId() != null ? request.contestId() : team.getContestId();
        final Long trackIdToValidate = request.trackId() != null ? request.trackId() : team.getTrackId();
        if (trackIdToValidate != null) {
            contestTrackConvenience.getValidateExistTrack(contestIdToValidate, trackIdToValidate);
        }
    }

    private void updateTeamByMember(final Long memberId, final Team team, final TeamUpdateRequest request) {
        teamMemberConvenience.getValidateExistTeamMember(team.getId(), memberId);
        validateContestAndTrackNotChanged(team, request);
        validateRequiredField(team, request);
        team.update(request);
        team.submit();
    }

    private void validateContestAndTrackNotChanged(final Team team, final TeamUpdateRequest request) {
        if (request.contestId() != null && !request.contestId().equals(team.getContestId())) {
            throw new TeamException(FORBIDDEN_CONTEST_OR_TRACK_UPDATE);
        }
        if (request.trackId() != null && !request.trackId().equals(team.getTrackId())) {
            throw new TeamException(FORBIDDEN_CONTEST_OR_TRACK_UPDATE);
        }
    }

    private void validateRequiredField(final Team team, final TeamUpdateRequest request) {
        final ContestTemplate template = contestTemplateConvenience.getValidateExistTemplate(team.getContestId());

        validateField(template.getTrackRequired(),
                request.trackId() != null ? request.trackId().toString() : null,
                team.getTrackId() != null ? team.getTrackId().toString() : null);
        validateField(template.getProjectNameRequired(), request.projectName(), team.getProjectName());
        validateField(template.getTeamNameRequired(), request.teamName(), team.getTeamName());
        validateField(template.getProfessorRequired(), request.professorName(), team.getProfessorName());
        validateField(template.getGithubPathRequired(), request.githubPath(), team.getGithubPath());
        validateField(template.getYouTubePathRequired(), request.youTubePath(), team.getYouTubePath());
        validateField(template.getProductionPathRequired(), request.productionPath(), team.getProductionPath());
        validateField(template.getOverviewRequired(), request.overview(), team.getOverview());
    }

    private void validateField(final boolean required, final String requestValue, final String currentValue) {
        final String resolvedValue = requestValue != null ? requestValue : currentValue;
        if (required && (resolvedValue == null || resolvedValue.isBlank())) {
            throw new TeamException(REQUIRED_FIELD_MISSING);
        }
    }

    public void savePreviewImages(final Long teamId, final List<MultipartFile> images, final Member member) {
        teamConvenience.validateExistTeam(teamId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        checkPreviewLimit(teamId, images);
        for (MultipartFile image : images) {
            fileCommandService.storeImageFile(image, teamId, TEAM, PREVIEW);
        }
    }

    public void deletePreviewImages(final Long teamId, final List<Long> ids, final Member member) {
        teamConvenience.validateExistTeam(teamId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        ids.forEach(fileCommandService::deleteFile);
    }

    public void saveThumbnailImage(final Long teamId, final MultipartFile image, final Member member) {
        teamConvenience.validateExistTeam(teamId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        fileCommandService.replaceImageFile(image, teamId, TEAM, THUMBNAIL);
    }

    public void deleteThumbnailImage(final Long teamId, final Member member) {
        teamConvenience.validateExistTeam(teamId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        deleteIfExists(teamId, THUMBNAIL);
    }

    public void savePosterImage(final Long teamId, final MultipartFile image, final Member member) {
        teamConvenience.validateExistTeam(teamId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        fileCommandService.replaceImageFile(image, teamId, TEAM, POSTER);
    }

    public void deletePosterImage(final Long teamId, final Member member) {
        teamConvenience.validateExistTeam(teamId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
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
                .ifPresent(existingFile -> fileCommandService.deleteFile(existingFile.getId()));
    }

    private void checkPreviewLimit(final Long teamId, final List<MultipartFile> images) {
        long savedCount = fileRepository.countByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, PREVIEW);
        if (savedCount + images.size() > 5) {
            throw new FileException(EXCEED_PREVIEW_LIMIT);
        }
    }
}
