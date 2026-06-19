package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.modules.file.application.FileQueryService;
import com.opus.opus.modules.contest.application.convenience.ContestAwardConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestAward;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.file.application.convenience.FileImageConvenience;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamLikeConvenience;
import com.opus.opus.modules.team.application.convenience.TeamVoteConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.TeamContestAwardResponse;
import com.opus.opus.modules.team.application.dto.response.TeamDetailResponse;
import com.opus.opus.modules.team.application.dto.response.TeamMemberResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamContestAward;
import com.opus.opus.modules.team.domain.TeamMember;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import com.opus.opus.modules.file.application.dto.FileResource;
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
    private final FileImageConvenience fileImageConvenience;

    private final FileQueryService fileQueryService;

    public ImageResponse getPreviewImage(final Long teamId, final Long imageId) {
        teamConvenience.validateExistTeam(teamId);
        final FileImage findFileImage = fileImageConvenience.findByFileImageId(imageId);
        checkImageConverted(findFileImage);
        final FileResource storageResult = fileQueryService.findFileAndType(findFileImage.getFile().getId());
        return new ImageResponse(storageResult.resource(), storageResult.mimeType());
    }

    public TeamDetailResponse getTeamDetailPublic(final Long teamId) {
        return getTeamDetail(teamId, null);
    }

    public TeamDetailResponse getTeamDetail(final Long teamId, final Member member) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);
        final Contest contest = contestConvenience.getValidateExistContest(team.getContestId());
        final ContestTrack track = team.getTrackId() != null
                ? contestTrackConvenience.getValidateExistTrack(team.getContestId(), team.getTrackId())
                : null;

        final List<TeamMemberResponse> teamMemberResponses = getTeamMemberResponses(team);
        final List<TeamContestAwardResponse.AwardInfo> awards = getAwardInfos(team);
        final List<Long> previewIds = fileImageConvenience.findAllPreviewFileIdsByTeamId(teamId);

        final Boolean isVoted = teamVoteConvenience.getIsVotedIfInPeriod(team, member, contest.isVotingPeriod());
        final Boolean isLiked = teamLikeConvenience.getIsLikedIfInPeriod(team, member, contest.isVotingPeriod());

        return TeamDetailResponse.of(
                team,
                contest.getContestName(),
                track != null ? track.getTrackName() : null,
                teamMemberResponses,
                previewIds,
                awards,
                isLiked,
                isVoted
        );
    }

    private List<TeamContestAwardResponse.AwardInfo> getAwardInfos(final Team team) {
        final List<Long> awardIds = team.getTeamAwards().stream()
                .map(TeamContestAward::getContestAwardId)
                .toList();
        final List<ContestAward> contestAwards = contestAwardConvenience.findAllById(awardIds);
        return contestAwards.stream()
                .map(TeamContestAwardResponse.AwardInfo::from)
                .toList();
    }

    private List<TeamMemberResponse> getTeamMemberResponses(final Team team) {
        final List<TeamMember> teamMembers = team.getTeamMembers();

        final List<Long> memberIds = teamMembers.stream()
                .map(TeamMember::getMemberId)
                .distinct()
                .toList();

        final Map<Long, Member> memberMap = memberConvenience.findAllByIdIncludingDeleted(memberIds)
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
        return fileImageConvenience.findOptionalByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, THUMBNAIL)
                .map(this::getImageResponse);
    }

    private Optional<ImageResponse> getTrackThumbnail(final Long trackId) {
        if (trackId == null) {
            return Optional.empty();
        }
        return fileImageConvenience.findOptionalByReferenceIdAndReferenceTypeAndImageType(trackId, TRACK, THUMBNAIL)
                .map(this::getImageResponse);
    }

    private ImageResponse getDefaultThumbnailResponse() {
        final FileResource defaultResult = fileQueryService.findDefaultThumbnail();
        return new ImageResponse(defaultResult.resource(), defaultResult.mimeType());
    }

    public ImageResponse getPosterImage(final Long teamId) {
        return getImage(teamId, POSTER);
    }

    private ImageResponse getImage(final Long teamId, final FileImageType fileImageType) {
        teamConvenience.validateExistTeam(teamId);
        final FileImage findFileImage = fileImageConvenience.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM,
                fileImageType);
        return getImageResponse(findFileImage);
    }

    private ImageResponse getImageResponse(final FileImage findFileImage) {
        checkImageConverted(findFileImage);
        final FileResource storageResult = fileQueryService.findFileAndType(findFileImage.getFile().getId());
        return new ImageResponse(storageResult.resource(), storageResult.mimeType());
    }

    private void checkImageConverted(final FileImage findFileImage) {
        if (!findFileImage.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
