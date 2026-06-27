package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestSubmissionArchiveQueryService;
import com.opus.opus.modules.contest.application.ContestSubmissionFileQueryService;
import com.opus.opus.modules.contest.application.dto.request.ArchiveRequest;
import com.opus.opus.modules.contest.application.dto.response.ArchiveTargetsResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionArchive;
import com.opus.opus.global.util.ContentDispositionUtil;
import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}/submissions")
@Secured("ROLE_관리자")
public class ContestSubmissionFileController {

    private final ContestSubmissionFileQueryService contestSubmissionFileQueryService;
    private final ContestSubmissionArchiveQueryService contestSubmissionArchiveQueryService;

    @GetMapping("/archives")
    public ResponseEntity<ArchiveTargetsResponse> getArchiveTargets(
            @PathVariable final Long contestId,
            @RequestParam(required = false) final Long submissionTypeId,
            @RequestParam(required = false) final Long trackId
    ) {
        return ResponseEntity.ok(contestSubmissionArchiveQueryService.getArchiveTargets(contestId, submissionTypeId, trackId));
    }

    @PostMapping("/archives")
    public ResponseEntity<StreamingResponseBody> downloadArchive(
            @PathVariable final Long contestId,
            @Valid @RequestBody final ArchiveRequest request
    ) {
        final SubmissionArchive submissionArchive = contestSubmissionArchiveQueryService.generateArchive(contestId, request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDispositionUtil.attachment(submissionArchive.fileName()))
                .body(submissionArchive.body());
    }

    @GetMapping("/{submissionId}/files/{fileId}")
    public ResponseEntity<Resource> downloadSubmissionFile(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @PathVariable final Long fileId
    ) {
        final DocumentFileDownload download = contestSubmissionFileQueryService.downloadSubmissionFile(contestId, submissionId, fileId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDispositionUtil.attachment(download.fileName()))
                .contentLength(download.fileSize())
                .body(download.resource());
    }
}
