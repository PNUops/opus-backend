package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_PREVIEW;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.response.TeamDetailResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamMemberResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamLikeConvenience;
import com.opus.opus.modules.team.application.convenience.TeamVoteConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.TeamMember;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final MemberConvenience memberConvenience;
    private final TeamVoteConvenience teamVoteConvenience;
    private final TeamLikeConvenience teamLikeConvenience;
    private final ContestAwardConvenience contestAwardConvenience;
    private final TeamConvenience teamConvenience;
    private final FileConvenience fileConvenience;

    private final FileRepository fileRepository;

    private final FileStorageUtil fileStorageUtil;

    public ImageResponse getPreviewImage(final Long teamId, final Long imageId) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileRepository.findById(imageId).orElseThrow(() -> new FileException(NOT_EXISTS_PREVIEW));
        checkImageConverted(findFile);
        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    public TeamDetailResponse getTeamDetailPublic(final Long teamId) {
        return getTeamDetail(teamId, null);
    }

    public TeamDetailResponse getTeamDetail(final Long teamId, final Member member) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        final Contest contest = contestConvenience.getValidateExistContest(team.getContestId());
        final ContestTrack track = contestTrackConvenience.getValidateExistTrack(team.getContestId(),
                team.getTrackId());

        final List<TeamMemberResponse> teamMemberResponses = getTeamMemberResponses(team);
        final TeamContestAwardResponse teamContestAwardResponse = getTeamContestAwardResponse(team);
        final List<Long> previewIds = fileConvenience.findAllPreviewIdsByTeamId(teamId);

        final Boolean isVoted = teamVoteConvenience.getIsVotedIfInPeriod(team, member, contest.isVotingPeriod());
        final Boolean isLiked = teamLikeConvenience.getIsLikedIfInPeriod(team, member, contest.isVotingPeriod());

        return TeamDetailResponse.of(
                team,
                contest.getContestName(),
                track.getTrackName(),
                teamMemberResponses,
                previewIds,
                teamContestAwardResponse,
                isLiked,
                isVoted
        );
    }

    private TeamContestAwardResponse getTeamContestAwardResponse(final Team team) {
        final List<Long> awardIds = team.getTeamAwards().stream()
                .map(TeamContestAward::getContestAwardId)
                .toList();
        final List<ContestAward> contestAwards = contestAwardConvenience.findAllById(awardIds);
        return TeamContestAwardResponse.from(contestAwards);
    }

    private List<TeamMemberResponse> getTeamMemberResponses(final Team team) {
        final List<TeamMember> teamMembers = team.getTeamMembers();

        final List<Long> memberIds = teamMembers.stream()
                .map(TeamMember::getMemberId)
                .distinct()
                .toList();

        final Map<Long, Member> memberMap = memberConvenience.findAllById(memberIds)
                .stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        return teamMembers.stream()
                .map(tm -> TeamMemberResponse.of(tm, memberMap.get(tm.getMemberId())))
                .toList();
    }

    public ImageResponse getThumbnailImage(final Long teamId) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);

        return getTeamThumbnail(teamId)
                .or(() -> getTrackThumbnail(team.getTrackId()))
                .orElseGet(this::getDefaultThumbnailResponse);
    }

    private Optional<ImageResponse> getTeamThumbnail(final Long teamId) {
        return fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, THUMBNAIL)
                .map(this::getImageResponse);
    }

    private Optional<ImageResponse> getTrackThumbnail(final Long trackId) {
        if (trackId == null) {
            return Optional.empty();
        }
        return fileRepository.findByReferenceIdAndReferenceTypeAndImageType(trackId, TRACK, THUMBNAIL)
                .map(this::getImageResponse);
    }

    private ImageResponse getDefaultThumbnailResponse() {
        final Pair<Resource, String> defaultResult = fileStorageUtil.findDefaultThumbnail();
        return new ImageResponse(defaultResult.a, defaultResult.b);
    }

    public ImageResponse getPosterImage(final Long teamId) {
        return getImage(teamId, POSTER);
    }

    private ImageResponse getImage(final Long teamId, final FileImageType fileImageType) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileConvenience.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM,
                fileImageType);
        return getImageResponse(findFile);
    }

    private ImageResponse getImageResponse(final File findFile) {
        checkImageConverted(findFile);
        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    private void checkImageConverted(final File findFile) {
        if (!findFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
