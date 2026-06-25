package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_SUBMISSION_FILE_FORMAT;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_FOUND_SUBMISSION;
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
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.application.FileDocumentCommandService;
import com.opus.opus.modules.file.application.convenience.FileDocumentConvenience;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
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
    private final ContestSubmissionRepository contestSubmissionRepository;
    private final FileDocumentCommandService fileDocumentCommandService;
    private final FileDocumentConvenience fileDocumentConvenience;

    public SubmissionCreateResponse createSubmission(final Long contestId, final Long submissionItemId,
                                                     final Long teamId, final List<MultipartFile> files,
                                                     final Member member) {
        contestConvenience.validateExistContest(contestId);
        teamConvenience.validateExistTeam(teamId);
        final ContestSubmissionItem submissionItem =
                contestSubmissionItemConvenience.getValidateExistSubmissionItem(submissionItemId);

        validateSubmission(contestId, teamId, submissionItem, files, member);

        final ContestSubmission submission = contestSubmissionRepository.save(
                ContestSubmission.create(teamId, submissionItem));
        storeFiles(submission.getId(), files);

        return new SubmissionCreateResponse(submission.getId());
    }

    public void addFiles(final Long contestId, final Long submissionId, final List<MultipartFile> files,
                         final Member member) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission = contestSubmissionConvenience.getValidateExistSubmission(submissionId);
        final ContestSubmissionItem submissionItem = getValidatedSubmissionItem(submission, contestId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(submission.getTeamId(), member);

        final List<FileDocument> existingFiles = fileDocumentConvenience.findAllBySubmissionId(submissionId);
        validateFiles(submissionItem, files, existingFiles.size() + files.size());
        validateSubmittable(submissionItem);

        appendFiles(submissionId, existingFiles, files);
    }

    public void deleteFile(final Long contestId, final Long submissionId, final Long fileId, final Member member) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission = contestSubmissionConvenience.getValidateExistSubmission(submissionId);
        final ContestSubmissionItem submissionItem = getValidatedSubmissionItem(submission, contestId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(submission.getTeamId(), member);

        final List<FileDocument> files = fileDocumentConvenience.findAllBySubmissionId(submissionId);
        validateFileBelongsToSubmission(files, fileId);
        validateSubmittable(submissionItem);

        fileDocumentCommandService.deleteDocumentFile(fileId);
        if (files.size() == 1) {
            contestSubmissionRepository.delete(submission);
        }
    }

    private void validateSubmission(final Long contestId, final Long teamId,
                                    final ContestSubmissionItem submissionItem, final List<MultipartFile> files,
                                    final Member member) {
        validateSubmissionItemInContest(submissionItem, contestId);
        teamMemberConvenience.validateTeamMemberUnlessAdmin(teamId, member);
        validateNotAlreadySubmitted(teamId, submissionItem);
        validateFiles(submissionItem, files, files.size());
        validateSubmittable(submissionItem);
    }

    private void validateFiles(final ContestSubmissionItem submissionItem, final List<MultipartFile> files,
                               final int totalFileCount) {
        validateFileCount(submissionItem, totalFileCount);
        validateFileFormats(submissionItem, files);
        validateFileSizes(submissionItem, files);
    }

    private ContestSubmissionItem getValidatedSubmissionItem(final ContestSubmission submission, final Long contestId) {
        final ContestSubmissionItem submissionItem = submission.getSubmissionItem();
        if (!submissionItem.isInContest(contestId)) {
            throw new ContestException(NOT_FOUND_SUBMISSION);
        }
        return submissionItem;
    }

    private void validateSubmissionItemInContest(final ContestSubmissionItem submissionItem, final Long contestId) {
        if (!submissionItem.isInContest(contestId)) {
            throw new ContestException(NOT_FOUND_SUBMISSION_ITEM);
        }
    }

    private void validateNotAlreadySubmitted(final Long teamId, final ContestSubmissionItem submissionItem) {
        if (contestSubmissionConvenience.existsSubmission(teamId, submissionItem)) {
            throw new ContestException(SUBMISSION_ALREADY_EXISTS);
        }
    }

    private void validateFileBelongsToSubmission(final List<FileDocument> files, final Long fileId) {
        final boolean exists = files.stream().anyMatch(file -> file.getId().equals(fileId));
        if (!exists) {
            throw new FileException(FileExceptionType.NOT_FOUND);
        }
    }

    private void validateFileCount(final ContestSubmissionItem submissionItem, final int totalFileCount) {
        if (totalFileCount > submissionItem.getMaxFileCount()) {
            throw new ContestException(SUBMISSION_FILE_COUNT_EXCEEDED);
        }
    }

    private void validateFileFormats(final ContestSubmissionItem submissionItem, final List<MultipartFile> files) {
        for (final MultipartFile file : files) {
            validateFileFormat(submissionItem, file.getOriginalFilename());
        }
    }

    private void validateFileSizes(final ContestSubmissionItem submissionItem, final List<MultipartFile> files) {
        final long maxFileSizeBytes = submissionItem.getMaxFileSizeMb() * MB_IN_BYTES;
        for (final MultipartFile file : files) {
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

    private void appendFiles(final Long submissionId, final List<FileDocument> existingFiles,
                             final List<MultipartFile> files) {
        final int startOrder = existingFiles.stream()
                .mapToInt(FileDocument::getFileOrder)
                .max()
                .orElse(0);
        for (int i = 0; i < files.size(); i++) {
            fileDocumentCommandService.storeDocumentFile(files.get(i), submissionId, startOrder + 1 + i);
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
