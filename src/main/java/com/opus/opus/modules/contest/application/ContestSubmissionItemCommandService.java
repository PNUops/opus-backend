package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionItemExceptionType.INVALID_SUBMISSION_PERIOD;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.ContestTrack;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionItemRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionItemException;
import java.time.LocalDateTime;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestSubmissionItemCommandService {

    private final ContestSubmissionItemRepository contestSubmissionItemRepository;
    private final ContestSubmissionRepository contestSubmissionRepository;

    private final ContestConvenience contestConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;

    public void createSubmissionItem(final Long contestId, final ContestSubmissionItemRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        final ContestTrack contestTrack = resolveContestTrack(contestId, request.contestTrackId());
        validateSubmissionPeriod(request.startAt(), request.endAt());
        contestSubmissionItemRepository.save(buildSubmissionItem(request, contest, contestTrack));
    }

    public void updateSubmissionItem(final Long contestId, final Long submissionItemId,
                                     final ContestSubmissionItemRequest request) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        final ContestTrack contestTrack = resolveContestTrack(contestId, request.contestTrackId());
        validateSubmissionPeriod(request.startAt(), request.endAt());
        submissionItem.updateContestSubmissionItem(
                request.name(), request.description(), new HashSet<>(request.allowedFileFormats()),
                request.maxFileSizeMb(), request.maxFileCount(), request.startAt(), request.endAt(),
                request.allowLateSubmission(), request.visibility(), contestTrack);
    }

    public void deleteSubmissionItem(final Long contestId, final Long submissionItemId) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(contestId, submissionItemId);
        contestSubmissionRepository.deleteAllBySubmissionItemId(submissionItemId);
        contestSubmissionItemRepository.delete(submissionItem);
    }

    private ContestTrack resolveContestTrack(final Long contestId, final Long contestTrackId) {
        if (contestTrackId == null) {
            return null;
        }
        return contestTrackConvenience.getValidateExistTrack(contestId, contestTrackId);
    }

    private void validateSubmissionPeriod(final LocalDateTime startAt, final LocalDateTime endAt) {
        if (startAt.isAfter(endAt)) {
            throw new ContestSubmissionItemException(INVALID_SUBMISSION_PERIOD);
        }
    }

    private ContestSubmissionItem buildSubmissionItem(final ContestSubmissionItemRequest request,
                                                      final Contest contest, final ContestTrack contestTrack) {
        return ContestSubmissionItem.builder()
                .name(request.name())
                .description(request.description())
                .allowedFileFormats(new HashSet<>(request.allowedFileFormats()))
                .maxFileSizeMb(request.maxFileSizeMb())
                .maxFileCount(request.maxFileCount())
                .startAt(request.startAt())
                .endAt(request.endAt())
                .allowLateSubmission(request.allowLateSubmission())
                .visibility(request.visibility())
                .contest(contest)
                .contestTrack(contestTrack)
                .build();
    }
}
