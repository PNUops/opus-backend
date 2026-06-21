package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.COMMENT_NOT_BELONG_TO_SUBMISSION;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOTHING_TO_UPDATE;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOT_FOUND_COMMENT;
import static com.opus.opus.modules.contest.exception.ContestSubmissionCommentExceptionType.NOT_OWNER_COMMENT;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestSubmissionComment;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionCommentRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionCommentException;
import com.opus.opus.modules.file.application.FileCommentCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class ContestSubmissionCommentCommandService {

    private final ContestSubmissionCommentRepository contestSubmissionCommentRepository;

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final FileCommentCommandService fileCommentCommandService;

    public void createComment(final Long contestId, final Long submissionId, final Long memberId,
                              final String description, final List<MultipartFile> files) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission = contestSubmissionConvenience.getValidateExistSubmission(submissionId);

        final ContestSubmissionComment comment = contestSubmissionCommentRepository.save(ContestSubmissionComment.builder()
                .description(description)
                .memberId(memberId)
                .submission(submission)
                .build());
        fileCommentCommandService.storeCommentFiles(files, comment.getId());
    }

    public void updateComment(final Long contestId, final Long submissionId, final Long commentId, final Long memberId,
                              final String description, final List<MultipartFile> addFiles,
                              final List<Long> removeFileIds) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);
        final ContestSubmissionComment comment = getValidateExistComment(commentId);

        validateCommentBelongsToSubmission(comment, submissionId);
        validateOwner(comment, memberId);
        validateNotBlankUpdate(description, addFiles, removeFileIds);

        fileCommentCommandService.deleteCommentFiles(removeFileIds, commentId);
        fileCommentCommandService.storeCommentFiles(addFiles, commentId);
        if (description != null && !description.isBlank()) {
            comment.updateDescription(description);
        }
    }

    public void deleteComment(final Long contestId, final Long submissionId, final Long commentId, final Long memberId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);
        final ContestSubmissionComment comment = getValidateExistComment(commentId);

        validateCommentBelongsToSubmission(comment, submissionId);
        validateOwner(comment, memberId);

        fileCommentCommandService.deleteAllByCommentId(commentId);
        contestSubmissionCommentRepository.delete(comment);
    }

    private ContestSubmissionComment getValidateExistComment(final Long commentId) {
        return contestSubmissionCommentRepository.findById(commentId)
                .orElseThrow(() -> new ContestSubmissionCommentException(NOT_FOUND_COMMENT));
    }

    private void validateCommentBelongsToSubmission(final ContestSubmissionComment comment, final Long submissionId) {
        if (!comment.getSubmission().getId().equals(submissionId)) {
            throw new ContestSubmissionCommentException(COMMENT_NOT_BELONG_TO_SUBMISSION);
        }
    }

    private void validateOwner(final ContestSubmissionComment comment, final Long memberId) {
        if (!comment.isOwner(memberId)) {
            throw new ContestSubmissionCommentException(NOT_OWNER_COMMENT);
        }
    }

    private void validateNotBlankUpdate(final String description, final List<MultipartFile> addFiles,
                                        final List<Long> removeFileIds) {
        final boolean noDescription = description == null || description.isBlank();
        final boolean noAddFiles = addFiles == null || addFiles.isEmpty();
        final boolean noRemoveFiles = removeFileIds == null || removeFileIds.isEmpty();
        if (noDescription && noAddFiles && noRemoveFiles) {
            throw new ContestSubmissionCommentException(NOTHING_TO_UPDATE);
        }
    }
}
