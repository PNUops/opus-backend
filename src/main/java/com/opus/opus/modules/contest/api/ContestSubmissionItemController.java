package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestSubmissionItemCommandService;
import com.opus.opus.modules.contest.application.ContestSubmissionItemQueryService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}/submission-items")
public class ContestSubmissionItemController {

    private final ContestSubmissionItemCommandService contestSubmissionItemCommandService;
    private final ContestSubmissionItemQueryService contestSubmissionItemQueryService;

    @Secured("ROLE_관리자")
    @PostMapping
    public ResponseEntity<Void> createSubmissionItem(@Valid @RequestBody final ContestSubmissionItemRequest request,
                                                     @PathVariable final Long contestId) {
        contestSubmissionItemCommandService.createSubmissionItem(contestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/{submissionItemId}")
    public ResponseEntity<Void> updateSubmissionItem(@Valid @RequestBody final ContestSubmissionItemRequest request,
                                                     @PathVariable final Long contestId,
                                                     @PathVariable final Long submissionItemId) {
        contestSubmissionItemCommandService.updateSubmissionItem(contestId, submissionItemId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/{submissionItemId}")
    public ResponseEntity<Void> deleteSubmissionItem(@PathVariable final Long contestId,
                                                     @PathVariable final Long submissionItemId) {
        contestSubmissionItemCommandService.deleteSubmissionItem(contestId, submissionItemId);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @GetMapping("/{submissionItemId}")
    public ResponseEntity<ContestSubmissionItemResponse> getSubmissionItem(@PathVariable final Long contestId,
                                                                           @PathVariable final Long submissionItemId) {
        return ResponseEntity.ok(contestSubmissionItemQueryService.getSubmissionItem(contestId, submissionItemId));
    }
}
