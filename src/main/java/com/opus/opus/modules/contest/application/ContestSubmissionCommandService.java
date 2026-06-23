package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_FILE_FORMAT;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION_ITEM;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_ALREADY_EXISTS;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_FILE_COUNT_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_FILE_SIZE_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.SUBMISSION_PERIOD_ENDED;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionItemConvenience;
import com.opus.opus.modules.contest.application.dto.response.SubmissionCreateResponse;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionItem;
import com.opus.opus.modules.contest.domain.SubmissionFileFormat;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.application.FileDocumentCommandService;
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

    private static final long MB_IN_BYTES = 1024L * 1024L;

    private final ContestConvenience contestConvenience;
    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final ContestSubmissionItemConvenience contestSubmissionItemConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final FileDocumentCommandService fileDocumentCommandService;

    public SubmissionCreateResponse createSubmission(final Long contestId, final Long submissionItemId,
                                                     final Long teamId, final List<MultipartFile> files,
                                                     final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.validateExistTeam(teamId);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(submissionItemId);

        validateSubmission(contestId, teamId, submissionItem, files, member);

        final ContestSubmission submission = contestSubmissionConvenience.save(
                ContestSubmission.create(teamId, submissionItem));
        storeFiles(submission.getId(), files);

        return new SubmissionCreateResponse(submission.getId());
    }

    private void validateSubmission(final Long contestId, final Long teamId,
                                    final ContestSubmissionItem submissionItem, final List<MultipartFile> files,
                                    final Member member) {
        validateSubmissionItemInContest(submissionItem, contestId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        validateNotAlreadySubmitted(teamId, submissionItem);
        validateFiles(submissionItem, files);
        validateSubmittable(submissionItem);
    }

    private void validateSubmissionItemInContest(final ContestSubmissionItem submissionItem, final Long contestId) {
        if (!submissionItem.getContest().getId().equals(contestId)) {
            throw new ContestException(NOT_FOUND_SUBMISSION_ITEM);
        }
    }

    private void validateNotAlreadySubmitted(final Long teamId, final ContestSubmissionItem submissionItem) {
        if (contestSubmissionConvenience.existsSubmission(teamId, submissionItem)) {
            throw new ContestException(SUBMISSION_ALREADY_EXISTS);
        }
    }

    private void validateFiles(final ContestSubmissionItem submissionItem, final List<MultipartFile> files) {
        if (files.size() > submissionItem.getMaxFileCount()) {
            throw new ContestException(SUBMISSION_FILE_COUNT_EXCEEDED);
        }
        final long maxFileSizeBytes = submissionItem.getMaxFileSizeMb() * MB_IN_BYTES;
        for (final MultipartFile file : files) {
            validateFileFormat(submissionItem, file.getOriginalFilename());
            if (file.getSize() > maxFileSizeBytes) {
                throw new ContestException(SUBMISSION_FILE_SIZE_EXCEEDED);
            }
        }
    }

    private void validateFileFormat(final ContestSubmissionItem submissionItem, final String filename) {
        final String extension = extractExtension(filename);
        final SubmissionFileFormat format;
        try {
            format = SubmissionFileFormat.valueOf(extension.toUpperCase());
        } catch (final IllegalArgumentException e) {
            throw new ContestException(INVALID_SUBMISSION_FILE_FORMAT);
        }
        if (!submissionItem.getAllowedFileFormats().contains(format)) {
            throw new ContestException(INVALID_SUBMISSION_FILE_FORMAT);
        }
    }

    private void validateSubmittable(final ContestSubmissionItem submissionItem) {
        final boolean afterDeadline = LocalDateTime.now().isAfter(submissionItem.getEndAt());
        if (afterDeadline && !submissionItem.getAllowLateSubmission()) {
            throw new ContestException(SUBMISSION_PERIOD_ENDED);
        }
    }

    private void storeFiles(final Long submissionId, final List<MultipartFile> files) {
        for (int i = 0; i < files.size(); i++) {
            fileDocumentCommandService.storeDocumentFile(files.get(i), submissionId, i + 1);
        }
    }

    private String extractExtension(final String filename) {
        if (filename == null || filename.isBlank()) {
            throw new ContestException(INVALID_SUBMISSION_FILE_FORMAT);
        }
        final int lastDot = filename.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= filename.length() - 1) {
            throw new ContestException(INVALID_SUBMISSION_FILE_FORMAT);
        }
        return filename.substring(lastDot + 1);
    }
}
