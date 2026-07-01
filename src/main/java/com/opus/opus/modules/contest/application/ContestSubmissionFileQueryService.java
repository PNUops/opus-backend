package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.request.SubmissionDownloadRequest;
import com.opus.opus.modules.contest.application.dto.request.DownloadTargetRequest;
import com.opus.opus.modules.contest.application.dto.response.DownloadTargetResponse;
import com.opus.opus.modules.contest.application.dto.response.DownloadTargetsResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionDownload;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestSubmission;
import com.opus.opus.modules.contest.domain.dao.DownloadSubmissionRow;
import com.opus.opus.modules.contest.domain.dao.DownloadTargetResult;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.contest.exception.ContestExceptionType;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.application.convenience.FileDocumentConvenience;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.domain.dao.SubmissionFileInfo;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestSubmissionFileQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final FileDocumentConvenience fileDocumentConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;
    private final TeamConvenience teamConvenience;

    public DocumentFileDownload downloadSubmissionFile(final Long contestId, final Long submissionId, final Long fileId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.getValidateSubmissionInContest(contestId, submissionId);
        fileDocumentConvenience.validateFileBelongsToSubmission(submissionId, fileId);

        return fileDocumentQueryService.download(fileId);
    }

    public SubmissionDownload generateSubmissionDownload(final Long contestId, final Long submissionId) {
        contestConvenience.validateExistContest(contestId);
        final ContestSubmission submission = contestSubmissionConvenience.getValidateSubmissionInContest(contestId, submissionId);

        final List<SubmissionFileInfo> files = fileDocumentQueryService.findFilesBySubmissionIds(List.of(submissionId));
        final String teamName = teamConvenience.getValidateExistTeam(submission.getTeamId()).getTeamName();

        return new SubmissionDownload(generateZipFileName(teamName),
                buildZipFileBody(files, SubmissionFileInfo::fileName));
    }

    public DownloadTargetsResponse getDownloadTargets(final Long contestId, final Long submissionItemId, final Long trackId) {
        contestConvenience.validateExistContest(contestId);

        final List<DownloadTargetResponse> targets = contestSubmissionConvenience
                .getDownloadTargets(contestId, submissionItemId, trackId).stream()
                .map(this::toDownloadTargetResponse)
                .toList();

        return new DownloadTargetsResponse(targets);
    }

    public SubmissionDownload generateDownload(final Long contestId, final SubmissionDownloadRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);

        final List<DownloadSubmissionRow> submissions = contestSubmissionConvenience.getDownloadSubmissions(contestId).stream()
                .filter(submission -> matchesTarget(submission, request.targets()))
                .toList();
        if (submissions.isEmpty()) {
            throw new ContestException(ContestExceptionType.NO_SUBMISSIONS_TO_DOWNLOAD);
        }

        final Map<Long, String> teamNameBySubmissionId = submissions.stream()
                .collect(Collectors.toMap(DownloadSubmissionRow::submissionId, DownloadSubmissionRow::teamName));
        final List<SubmissionFileInfo> files = fileDocumentQueryService.findFilesBySubmissionIds(
                submissions.stream().map(DownloadSubmissionRow::submissionId).toList());
        if (files.isEmpty()) {
            throw new ContestException(ContestExceptionType.NO_SUBMISSIONS_TO_DOWNLOAD);
        }

        return new SubmissionDownload(generateZipFileName(contest.getContestName()),
                buildZipFileBody(files, file -> teamNameBySubmissionId.get(file.submissionId()) + "/" + file.fileName()));
    }

    private DownloadTargetResponse toDownloadTargetResponse(final DownloadTargetResult result) {
        return new DownloadTargetResponse(
                result.submissionItemId(),
                result.submissionItemName(),
                result.trackId(),
                result.trackName(),
                Math.toIntExact(result.submittedTeamCount()),
                result.estimatedSize());
    }

    private boolean matchesTarget(final DownloadSubmissionRow submission, final List<DownloadTargetRequest> targets) {
        return targets.stream().anyMatch(target ->
                target.submissionItemId().equals(submission.submissionItemId())
                        && (target.trackId() == null || target.trackId().equals(submission.trackId())));
    }

    private StreamingResponseBody buildZipFileBody(final List<SubmissionFileInfo> files,
                                                   final Function<SubmissionFileInfo, String> entryNameResolver) {
        return outputStream -> {
            final Set<String> usedEntryNames = new HashSet<>();
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                for (final SubmissionFileInfo file : files) {
                    final String entryName = deduplicateEntryName(usedEntryNames, entryNameResolver.apply(file));
                    zipOutputStream.putNextEntry(new ZipEntry(entryName));
                    try (InputStream inputStream = fileDocumentQueryService.openStream(file.fileDocumentId())) {
                        inputStream.transferTo(zipOutputStream);
                    }
                    zipOutputStream.closeEntry();
                }
            }
        };
    }

    // 만약에 같은 폴더에 동일 파일명이 들어오면 ZipException(duplicate entry)으로 다운로드가 깨지므로
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

    private String generateZipFileName(final String name) {
        final String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        return "%s_%s.zip".formatted(name.replaceAll("\\s+", ""), date);
    }
}
