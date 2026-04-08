package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;

import com.opus.opus.modules.file.application.FileCommandService;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestTrackRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestTrackRepository;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestTrackCommandService {

    private final ContestTrackRepository contestTrackRepository;
    private final FileRepository fileRepository;

    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestConvenience contestConvenience;
    private final TeamConvenience teamConvenience;

    private final FileCommandService fileCommandService;

    public void createTrack(final Long contestId, final ContestTrackRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        contestTrackConvenience.validateDuplicateTrackName(contestId, request.trackName());
        final ContestTrack contestTrack = ContestTrack.builder()
                .trackName(request.trackName())
                .contest(contest)
                .build();
        contestTrackRepository.save(contestTrack);
    }

    public void updateTrack(final Long contestId, final Long trackId, final ContestTrackRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        contestTrackConvenience.validateDuplicateTrackName(contestId, request.trackName());
        final ContestTrack contestTrack = contestTrackConvenience.getValidateExistTrack(contestId, trackId);
        contestTrack.updateTrack(contest, request.trackName());
    }

    public void deleteTrack(final Long contestId, final Long trackId) {
        final ContestTrack contestTrack = contestTrackConvenience.getValidateExistTrack(contestId, trackId);
        teamConvenience.validateAllTeamsDeletedInTrack(trackId);
        contestTrackRepository.delete(contestTrack);
    }

    public void saveContestTrackDefaultThumbnail(final Long contestId, final Long trackId, final MultipartFile image) {
        contestTrackConvenience.getValidateExistTrack(contestId, trackId);
        fileCommandService.replaceImageFile(image, trackId, TRACK, THUMBNAIL);
    }

    public void deleteContestTrackDefaultThumbnail(final Long contestId, final Long trackId) {
        contestTrackConvenience.getValidateExistTrack(contestId, trackId);
        deleteIfExists(trackId, THUMBNAIL);
    }

    private void deleteIfExists(final Long trackId, final FileImageType imageType) {
        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(trackId, TRACK, imageType)
                .ifPresent(existingFile -> fileCommandService.deleteFile(existingFile.getId()));
    }
}
