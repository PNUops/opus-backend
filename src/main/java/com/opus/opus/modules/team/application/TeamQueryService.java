package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_PREVIEW;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.domain.dao.TeamSubmissionResult;
import com.opus.opus.modules.team.domain.dao.TeamVoteRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamConvenience teamConvenience;
    private final FileConvenience fileConvenience;
    private final ContestConvenience contestConvenience;

    private final FileRepository fileRepository;
    private final TeamVoteRepository teamVoteRepository;
    private final TeamRepository teamRepository;

    private final FileStorageUtil fileStorageUtil;

    public ImageResponse getPreviewImage(final Long teamId, final Long imageId) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileRepository.findById(imageId).orElseThrow(() -> new FileException(NOT_EXISTS_PREVIEW));
        checkImageConverted(findFile);
        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    public ImageResponse getThumbnailImage(final Long teamId) {
        return getImage(teamId, THUMBNAIL);
    }

    public ImageResponse getPosterImage(final Long teamId) {
        return getImage(teamId, POSTER);
    }

    @Transactional(readOnly = true)
    public MemberVoteCountResponse getMemberVoteCount(Long memberId, Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final long currentVoteCount = teamVoteRepository.countMemberVotesInContest(memberId, contestId);
        final long remainingVotesCount = contest.getMaxVotesLimit() - currentVoteCount;
        return new MemberVoteCountResponse(remainingVotesCount, (long) contest.getMaxVotesLimit());
    }

    public List<ContestSubmissionResponse> getTeamSubmissions(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final List<TeamSubmissionResult> results = teamRepository.findTeamSubmissionsByContestId(contestId);
        return results.stream()
                .map(ContestSubmissionResponse::from)
                .toList();
    }

    private ImageResponse getImage(final Long teamId, final FileImageType fileImageType) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileConvenience.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, fileImageType);
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
