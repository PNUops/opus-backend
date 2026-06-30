package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestSubmissionFeedbackCommandService;
import com.opus.opus.modules.contest.application.ContestSubmissionFeedbackQueryService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionFeedbackSaveRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionFeedbackResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionMyFeedbackResponse;
import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.member.domain.Member;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}/submissions/{submissionId}/feedbacks")
public class ContestSubmissionFeedbackController {

    private final ContestSubmissionFeedbackCommandService contestSubmissionFeedbackCommandService;
    private final ContestSubmissionFeedbackQueryService contestSubmissionFeedbackQueryService;

    @PutMapping
    @Secured("ROLE_외부멘토")
    public ResponseEntity<Void> saveFeedback(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @Valid @ModelAttribute final ContestSubmissionFeedbackSaveRequest request,
            @RequestPart(required = false) final List<MultipartFile> files,
            @LoginMember final Member member
    ) {
        contestSubmissionFeedbackCommandService.saveFeedback(contestId, submissionId, member.getId(), request.description(), files, request.removeFileIds());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Secured("ROLE_외부멘토")
    public ResponseEntity<ContestSubmissionMyFeedbackResponse> getFeedback(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(contestSubmissionFeedbackQueryService.getFeedback(contestId, submissionId, member.getId()));
    }

    @GetMapping
    @Secured({"ROLE_관리자", "ROLE_학생"})
    public ResponseEntity<List<ContestSubmissionFeedbackResponse>> getFeedbacks(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(contestSubmissionFeedbackQueryService.getFeedbacks(contestId, submissionId, member));
    }

    @PatchMapping("/{feedbackId}/read")
    @Secured({"ROLE_학생"})
    public ResponseEntity<Void> markFeedbackAsRead(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @PathVariable final Long feedbackId,
            @RequestParam final Long teamId,
            @LoginMember final Member member
    ) {
        contestSubmissionFeedbackCommandService.markFeedbackAsRead(contestId, submissionId, feedbackId, teamId, member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{feedbackId}/files/{fileId}")
    @Secured({"ROLE_관리자", "ROLE_학생"})
    public ResponseEntity<Resource> downloadFeedbackFile(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @PathVariable final Long feedbackId,
            @PathVariable final Long fileId,
            @LoginMember final Member member
    ) {
        final FileDownload download = contestSubmissionFeedbackQueryService.downloadFeedbackFile(contestId, submissionId, feedbackId, fileId, member);

        final ContentDisposition contentDisposition = ContentDisposition.attachment()
                .filename(download.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType(download.mimeType()))
                .body(download.resource());
    }
}
