package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_FILE_FORMAT;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_FILE_COUNT_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_FILE_REQUIRED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_FILE_SIZE_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_PERIOD_ENDED;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.dto.response.SubmissionCreateResponse;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.application.FileDocumentCommandService;
import com.opus.opus.modules.file.application.convenience.FileDocumentConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestSubmissionCommandService {

    private final ContestSubmissionRepository contestSubmissionRepository;

    private final ContestConvenience contestConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final FileDocumentConvenience fileDocumentConvenience;

    private final FileDocumentCommandService fileDocumentCommandService;

    public SubmissionCreateResponse createSubmission(final Long contestId, final Long submissionItemId,
                                                     final Long teamId, final List<MultipartFile> files,
                                                     final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.getValidateTeamInContest(teamId, contestId);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(submissionItemId);

        validateSubmission(contestId, teamId, submissionItem, files, member);

        final ContestSubmission submission = contestSubmissionRepository.save(ContestSubmission.builder()
                .teamId(teamId)
                .firstSubmittedAt(LocalDateTime.now())
                .submissionItem(submissionItem)
                .build());
        fileDocumentCommandService.storeDocumentFiles(submission.getId(), files);

        return new SubmissionCreateResponse(submission.getId());
    }

    public void addSubmissionFiles(final Long contestId, final Long submissionId, final List<MultipartFile> files,
                                   final Member member) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission =
                contestSubmissionConvenience.getValidateSubmissionInContest(submissionId, contestId);
        final ContestSubmissionItem submissionItem = submission.getSubmissionItem();
        teamMemberConvenience.validateTeamMemberUnlessAdmin(submission.getTeamId(), member);

        validateSubmittable(submissionItem);
        validateFilesNotEmpty(files);
        final long existingFileCount = fileDocumentConvenience.countBySubmissionId(submissionId);
        validateFiles(submissionItem, files, (int) existingFileCount + files.size());

        fileDocumentCommandService.storeDocumentFiles(submissionId, files);
        contestSubmissionRepository.touchUpdatedAt(submissionId);
    }

    public void deleteSubmissionFile(final Long contestId, final Long submissionId, final Long fileId,
                                     final Member member) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission =
                contestSubmissionConvenience.getValidateSubmissionInContest(submissionId, contestId);
        final ContestSubmissionItem submissionItem = submission.getSubmissionItem();
        teamMemberConvenience.validateTeamMemberUnlessAdmin(submission.getTeamId(), member);

        fileDocumentConvenience.validateFileBelongsToSubmission(submissionId, fileId);
        validateSubmittable(submissionItem);

        final boolean isLastFile = fileDocumentConvenience.countBySubmissionId(submissionId) == 1;
        fileDocumentCommandService.deleteDocumentFile(fileId);
        if (isLastFile) {
            contestSubmissionRepository.delete(submission);
        } else {
            contestSubmissionRepository.touchUpdatedAt(submissionId);
        }
    }

    private void validateSubmission(final Long contestId, final Long teamId,
                                    final ContestSubmissionItem submissionItem, final List<MultipartFile> files,
                                    final Member member) {
        validateSubmissionItemInContest(submissionItem, contestId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        validateNotAlreadySubmitted(teamId, submissionItem);
        validateSubmittable(submissionItem);
        validateFilesNotEmpty(files);
        validateFiles(submissionItem, files, files.size());
    }

    private void validateFilesNotEmpty(final List<MultipartFile> files) {
        if (files.isEmpty()) {
            throw new ContestException(SUBMISSION_FILE_REQUIRED);
        }
        for (final MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new ContestException(SUBMISSION_FILE_REQUIRED);
            }
        }
    }

    private void validateFiles(final ContestSubmissionItem submissionItem, final List<MultipartFile> files,
                               final int totalFileCount) {
        validateFileCount(submissionItem, totalFileCount);
        validateFileFormats(submissionItem, files);
        validateFileSizes(submissionItem, files);
    }

    private void validateSubmissionItemInContest(final ContestSubmissionItem submissionItem, final Long contestId) {
        if (!submissionItem.isInContest(contestId)) {
            throw new ContestException(NOT_FOUND_SUBMISSION_ITEM);
        }
    }

    private void validateNotAlreadySubmitted(final Long teamId, final ContestSubmissionItem submissionItem) {
        if (contestSubmissionConvenience.isSubmitted(teamId, submissionItem)) {
            throw new ContestException(SUBMISSION_ALREADY_EXISTS);
        }
    }

    private void validateFileCount(final ContestSubmissionItem submissionItem, final int totalFileCount) {
        if (submissionItem.isFileCountExceeded(totalFileCount)) {
            throw new ContestException(SUBMISSION_FILE_COUNT_EXCEEDED);
        }
    }

    private void validateFileFormats(final ContestSubmissionItem submissionItem, final List<MultipartFile> files) {
        for (final MultipartFile file : files) {
            validateFileFormat(submissionItem, file.getOriginalFilename());
        }
    }

    private void validateFileSizes(final ContestSubmissionItem submissionItem, final List<MultipartFile> files) {
        for (final MultipartFile file : files) {
            if (submissionItem.isFileSizeExceeded(file.getSize())) {
                throw new ContestException(SUBMISSION_FILE_SIZE_EXCEEDED);
            }
        }
    }

    private void validateFileFormat(final ContestSubmissionItem submissionItem, final String filename) {
        final String extension = File.extractExtension(filename);
        final SubmissionFileFormat format = SubmissionFileFormat.fromExtension(extension)
                .orElseThrow(() -> new ContestException(INVALID_SUBMISSION_FILE_FORMAT));
        if (!submissionItem.isAllowedFormat(format)) {
            throw new ContestException(INVALID_SUBMISSION_FILE_FORMAT);
        }
    }

    private void validateSubmittable(final ContestSubmissionItem submissionItem) {
        if (submissionItem.isSubmissionClosed()) {
            throw new ContestException(SUBMISSION_PERIOD_ENDED);
        }
    }

}
