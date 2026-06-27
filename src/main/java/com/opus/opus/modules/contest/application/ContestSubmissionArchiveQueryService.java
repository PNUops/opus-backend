package com.opus.opus.modules.contest.application;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toMap;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTrackConvenience;
import com.opus.opus.modules.contest.application.dto.request.ArchiveRequest;
import com.opus.opus.modules.contest.application.dto.request.ArchiveTargetRequest;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetResponse;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetsResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionArchive;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.ContestTrack;
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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionArchiveQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final ContestTrackConvenience contestTrackConvenience;
    private final TeamConvenience teamConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;

    public ArchiveTargetsResponse getArchiveTargets(final Long contestId, final Long submissionTypeId, final Long trackId) {
        contestConvenience.validateExistContest(contestId);

        final List<ContestSubmission> submissions = contestSubmissionConvenience.getSubmissionsOfContest(contestId)
                .stream()
                .filter(submission -> submissionTypeId == null
                        || submission.getSubmissionItem().getId().equals(submissionTypeId))
                .toList();

        final Map<Long, Long> teamTrackMap = teamConvenience.getTeamsOfContest(contestId).stream()
                .filter(team -> team.getTrackId() != null)
                .collect(toMap(Team::getId, Team::getTrackId));
        final Map<Long, String> trackNameMap = contestTrackConvenience.getValidateExistTracks(contestId).stream()
                .collect(toMap(ContestTrack::getId, ContestTrack::getTrackName));
        final Map<Long, Long> sizeBySubmission = fileDocumentQueryService.getArchiveEntries(
                        submissions.stream().map(ContestSubmission::getId).toList()).stream()
                .collect(groupingBy(ArchiveFileEntry::submissionId, summingLong(ArchiveFileEntry::fileSize)));

        final Map<GroupKey, GroupData> groups = new LinkedHashMap<>();
        for (final ContestSubmission submission : submissions) {
            final Long teamTrackId = teamTrackMap.get(submission.getTeamId());
            if (teamTrackId == null || (trackId != null && !trackId.equals(teamTrackId))) {
                continue;
            }
            final GroupKey key = new GroupKey(submission.getSubmissionItem().getId(), teamTrackId);
            final GroupData data = groups.computeIfAbsent(key,
                    ignored -> new GroupData(submission.getSubmissionItem().getName()));
            data.teamIds.add(submission.getTeamId());
            data.estimatedSize += sizeBySubmission.getOrDefault(submission.getId(), 0L);
        }

        final List<ArchiveTargetResponse> archives = groups.entrySet().stream()
                .map(entry -> new ArchiveTargetResponse(
                        entry.getKey().submissionTypeId(),
                        entry.getValue().submissionTypeName,
                        entry.getKey().trackId(),
                        trackNameMap.get(entry.getKey().trackId()),
                        entry.getValue().teamIds.size(),
                        entry.getValue().estimatedSize))
                .toList();

        return new ArchiveTargetsResponse(archives);
    }

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
                plans.add(new ArchiveEntryPlan(folderName, entry.fileName(), entry.filePath()));
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
            final Set<String> usedEntryNames = new HashSet<>();
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                for (final ArchiveEntryPlan plan : plans) {
                    final String entryName = deduplicateEntryName(usedEntryNames,
                            plan.folderName() + "/" + plan.fileName());
                    zipOutputStream.putNextEntry(new ZipEntry(entryName));
                    try (InputStream inputStream = fileDocumentQueryService.openStream(plan.filePath())) {
                        inputStream.transferTo(zipOutputStream);
                    }
                    zipOutputStream.closeEntry();
                }
            }
        };
    }

    // 같은 폴더에 동일 파일명이 들어오면 ZipException(duplicate entry)으로 다운로드가 깨지므로
    // "이름(1).pdf"처럼 확장자 앞에 순번을 붙여 엔트리명 충돌을 회피한다.
    private String deduplicateEntryName(final Set<String> usedEntryNames, final String entryName) {
        if (usedEntryNames.add(entryName)) {
            return entryName;
        }
        final int extensionIndex = entryName.lastIndexOf('.');
        final boolean hasExtension = extensionIndex > entryName.lastIndexOf('/');
        final String base = hasExtension ? entryName.substring(0, extensionIndex) : entryName;
        final String extension = hasExtension ? entryName.substring(extensionIndex) : "";
        int sequence = 1;
        String candidate;
        do {
            candidate = base + "(" + sequence++ + ")" + extension;
        } while (!usedEntryNames.add(candidate));
        return candidate;
    }

    private String generateFileName(final Contest contest) {
        final String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        final String contestName = contest.getContestName().replaceAll("\\s+", "");
        return "%s_%s.zip".formatted(contestName, date);
    }

    private record GroupKey(Long submissionTypeId, Long trackId) {
    }

    private static final class GroupData {
        private final String submissionTypeName;
        private final Set<Long> teamIds = new HashSet<>();
        private long estimatedSize = 0L;

        private GroupData(final String submissionTypeName) {
            this.submissionTypeName = submissionTypeName;
        }
    }

    private record ArchiveEntryPlan(String folderName, String fileName, String filePath) {
    }
}
