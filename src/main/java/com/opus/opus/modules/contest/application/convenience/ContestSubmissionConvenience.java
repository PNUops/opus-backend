package com.opus.opus.modules.contest.application.convenience;

import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.dao.ArchiveFileRow;
import com.opus.opus.modules.contest.domain.dao.ArchiveTargetResult;
import com.opus.opus.modules.contest.domain.dao.ContestSubmissionRepository;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionConvenience {

    private final ContestSubmissionRepository contestSubmissionRepository;

    public void validateExistSubmission(final Long submissionId) {
        if (!contestSubmissionRepository.existsById(submissionId)) {
            throw new ContestSubmissionException(ContestSubmissionExceptionType.NOT_FOUND_SUBMISSION);
        }
    }

    public List<ContestSubmission> getSubmissionsOfContest(final Long contestId) {
        return contestSubmissionRepository.findAllByContestId(contestId);
    }

    public List<ArchiveTargetResult> getArchiveTargets(final Long contestId, final Long submissionTypeId,
                                                       final Long trackId) {
        return contestSubmissionRepository.findArchiveTargets(contestId, submissionTypeId, trackId);
    }

    public List<ArchiveFileRow> getArchiveFileRows(final Long contestId) {
        return contestSubmissionRepository.findArchiveFileRows(contestId);
    }
}
