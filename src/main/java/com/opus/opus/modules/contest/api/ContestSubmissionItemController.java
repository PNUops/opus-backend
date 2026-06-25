package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestSubmissionItemCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
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

    @Secured("ROLE_관리자")
    @PostMapping
    public ResponseEntity<Void> createSubmissionItem(@Valid @RequestBody final ContestSubmissionItemRequest request,
                                                     @PathVariable final Long contestId) {
        contestSubmissionItemCommandService.createSubmissionItem(contestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
