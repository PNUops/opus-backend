package com.opus.opus.modules.contest.application;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.request.ArchiveRequest;
import com.opus.opus.modules.contest.application.dto.request.ArchiveTargetRequest;
import com.opus.opus.modules.contest.application.dto.response.SubmissionArchive;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.application.dto.ArchiveFileEntry;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionArchiveCommandService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final TeamConvenience teamConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;

    public SubmissionArchive generateArchive(final Long contestId, final ArchiveRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);

        final Map<Long, Team> teamMap = teamConvenience.getTeamsOfContest(contestId).stream()
                .collect(toMap(Team::getId, team -> team));

        final List<ContestSubmission> matched = contestSubmissionConvenience.getSubmissionsOfContest(contestId).stream()
                .filter(submission -> matchesAnyTarget(submission, request.targets(), teamMap))
                .toList();

        final Map<Long, List<ArchiveFileEntry>> entriesBySubmission = fileDocumentQueryService.getArchiveEntries(
                        matched.stream().map(ContestSubmission::getId).toList()).stream()
                .collect(groupingBy(ArchiveFileEntry::submissionId));

        final List<ArchiveEntryPlan> plans = new ArrayList<>();
        for (final ContestSubmission submission : matched) {
            final Team team = teamMap.get(submission.getTeamId());
            final String folderName = team != null ? team.getTeamName() : "unknown";
            for (final ArchiveFileEntry entry : entriesBySubmission.getOrDefault(submission.getId(), List.of())) {
                plans.add(new ArchiveEntryPlan(folderName, entry.fileName(), entry.fileDocumentId()));
            }
        }

        if (plans.isEmpty()) {
            throw new ContestSubmissionException(ContestSubmissionExceptionType.NO_SUBMISSIONS_TO_ARCHIVE);
        }

        return new SubmissionArchive(generateFileName(contest), buildStreamingBody(plans));
    }

    private boolean matchesAnyTarget(final ContestSubmission submission, final List<ArchiveTargetRequest> targets,
                                     final Map<Long, Team> teamMap) {
        final Long submissionTypeId = submission.getSubmissionItem().getId();
        final Team team = teamMap.get(submission.getTeamId());
        final Long teamTrackId = team != null ? team.getTrackId() : null;
        return targets.stream().anyMatch(target -> target.submissionTypeId().equals(submissionTypeId)
                && (target.trackId() == null || target.trackId().equals(teamTrackId)));
    }

    private StreamingResponseBody buildStreamingBody(final List<ArchiveEntryPlan> plans) {
        return outputStream -> {
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                for (final ArchiveEntryPlan plan : plans) {
                    zipOutputStream.putNextEntry(new ZipEntry(plan.folderName() + "/" + plan.fileName()));
                    try (InputStream inputStream = fileDocumentQueryService.openDocumentStream(plan.fileDocumentId())) {
                        inputStream.transferTo(zipOutputStream);
                    }
                    zipOutputStream.closeEntry();
                }
            }
        };
    }

    private String generateFileName(final Contest contest) {
        final String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        final String contestName = contest.getContestName().replaceAll("\\s+", "");
        return "%s_%s.zip".formatted(contestName, date);
    }

    private record ArchiveEntryPlan(String folderName, String fileName, Long fileDocumentId) {
    }
}
