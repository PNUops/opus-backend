package com.opus.opus.modules.contest.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSubmissionConvenience;
import com.opus.opus.modules.contest.application.dto.request.SubmissionDownloadRequest;
import com.opus.opus.modules.contest.application.dto.request.DownloadTargetRequest;
import com.opus.opus.modules.contest.application.dto.response.DownloadTargetResponse;
import com.opus.opus.modules.contest.application.dto.response.DownloadTargetsResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionDownload;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.dao.DownloadFileRow;
import com.opus.opus.modules.contest.domain.dao.DownloadTargetResult;
import com.opus.opus.modules.contest.exception.ContestSubmissionException;
import com.opus.opus.modules.contest.exception.ContestSubmissionExceptionType;
import com.opus.opus.modules.file.application.FileDocumentQueryService;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
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
public class ContestSubmissionFileQueryService {

    private final ContestConvenience contestConvenience;
    private final ContestSubmissionConvenience contestSubmissionConvenience;
    private final FileDocumentQueryService fileDocumentQueryService;

    public DocumentFileDownload downloadSubmissionFile(final Long contestId, final Long submissionId, final Long fileId) {
        contestConvenience.validateExistContest(contestId);
        contestSubmissionConvenience.validateExistSubmission(submissionId);

        final DocumentFileDownload fileDownload = fileDocumentQueryService.download(fileId);
        if (!fileDownload.submissionId().equals(submissionId)) {
            throw new FileException(FileExceptionType.NOT_FOUND);
        }
        return fileDownload;
    }

    public DownloadTargetsResponse getDownloadTargets(final Long contestId, final Long submissionTypeId, final Long trackId) {
        contestConvenience.validateExistContest(contestId);

        final List<DownloadTargetResponse> targets = contestSubmissionConvenience
                .getDownloadTargets(contestId, submissionTypeId, trackId).stream()
                .map(this::toDownloadTargetResponse)
                .toList();

        return new DownloadTargetsResponse(targets);
    }

    public SubmissionDownload generateDownload(final Long contestId, final SubmissionDownloadRequest request) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);

        final List<DownloadFileRow> rows = contestSubmissionConvenience.getDownloadFileRows(contestId).stream()
                .filter(row -> matchesAnyTarget(row, request.targets()))
                .toList();

        if (rows.isEmpty()) {
            throw new ContestSubmissionException(ContestSubmissionExceptionType.NO_SUBMISSIONS_TO_DOWNLOAD);
        }

        return new SubmissionDownload(generateFileName(contest), buildStreamingBody(rows));
    }

    private DownloadTargetResponse toDownloadTargetResponse(final DownloadTargetResult result) {
        return new DownloadTargetResponse(
                result.submissionTypeId(),
                result.submissionTypeName(),
                result.trackId(),
                result.trackName(),
                Math.toIntExact(result.submittedTeamCount()),
                result.estimatedSize());
    }

    private boolean matchesAnyTarget(final DownloadFileRow row, final List<DownloadTargetRequest> targets) {
        return targets.stream().anyMatch(target -> target.submissionTypeId().equals(row.submissionTypeId())
                && (target.trackId() == null || target.trackId().equals(row.trackId())));
    }

    private StreamingResponseBody buildStreamingBody(final List<DownloadFileRow> rows) {
        return outputStream -> {
            final Set<String> usedEntryNames = new HashSet<>();
            try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
                for (final DownloadFileRow row : rows) {
                    final String entryName = deduplicateEntryName(usedEntryNames,
                            row.teamName() + "/" + row.fileName());
                    zipOutputStream.putNextEntry(new ZipEntry(entryName));
                    try (InputStream inputStream = fileDocumentQueryService.openStream(row.filePath())) {
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
}
