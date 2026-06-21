package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestSubmissionCommentCommandService;
import com.opus.opus.modules.contest.application.ContestSubmissionCommentQueryService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionCommentCreateRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionCommentUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionCommentResponse;
import com.opus.opus.modules.member.domain.Member;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}/submissions/{submissionId}/comments")
@Secured({"ROLE_관리자", "ROLE_교수", "ROLE_외부멘토"})
public class ContestSubmissionCommentController {

    private final ContestSubmissionCommentCommandService contestSubmissionCommentCommandService;
    private final ContestSubmissionCommentQueryService contestSubmissionCommentQueryService;

    @PostMapping
    public ResponseEntity<Void> createComment(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @Valid @ModelAttribute final ContestSubmissionCommentCreateRequest request,
            @RequestPart(required = false) final List<MultipartFile> files,
            @LoginMember final Member member
    ) {
        contestSubmissionCommentCommandService.createComment(contestId, submissionId, member.getId(), request.description(), files);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<ContestSubmissionCommentResponse>> getComments(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId
    ) {
        return ResponseEntity.ok(contestSubmissionCommentQueryService.getComments(contestId, submissionId));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<Void> updateComment(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @PathVariable final Long commentId,
            @Valid @ModelAttribute final ContestSubmissionCommentUpdateRequest request,
            @RequestPart(required = false) final List<MultipartFile> addFiles,
            @LoginMember final Member member
    ) {
        contestSubmissionCommentCommandService.updateComment(contestId, submissionId, commentId, member.getId(), request.description(), addFiles, request.removeFileIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable final Long contestId,
            @PathVariable final Long submissionId,
            @PathVariable final Long commentId,
            @LoginMember final Member member
    ) {
        contestSubmissionCommentCommandService.deleteComment(contestId, submissionId, commentId, member.getId());
        return ResponseEntity.noContent().build();
    }
}
