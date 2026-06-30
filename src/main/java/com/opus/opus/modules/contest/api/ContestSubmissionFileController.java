package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestSubmissionFileQueryService;
import com.opus.opus.modules.contest.application.dto.request.SubmissionDownloadRequest;
import com.opus.opus.modules.contest.application.dto.response.DownloadTargetsResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionDownload;
import com.opus.opus.global.util.FileDownloadUtil;
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

    @GetMapping("/downloads")
    public ResponseEntity<DownloadTargetsResponse> getDownloadTargets(
            @PathVariable final Long contestId,
            @RequestParam(required = false) final Long submissionItemId,
            @RequestParam(required = false) final Long trackId
    ) {
        return ResponseEntity.ok(contestSubmissionFileQueryService.getDownloadTargets(contestId, submissionItemId, trackId));
    }

    @PostMapping("/downloads")
    public ResponseEntity<StreamingResponseBody> downloadSubmissions(
            @PathVariable final Long contestId,
            @Valid @RequestBody final SubmissionDownloadRequest request
    ) {
        final SubmissionDownload submissionDownload = contestSubmissionFileQueryService.generateDownload(contestId, request);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, FileDownloadUtil.attachmentHeader(submissionDownload.fileName()))
                .body(submissionDownload.body());
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
                .header(HttpHeaders.CONTENT_DISPOSITION, FileDownloadUtil.attachmentHeader(download.fileName()))
                .contentLength(download.fileSize())
                .body(download.resource());
    }
}
