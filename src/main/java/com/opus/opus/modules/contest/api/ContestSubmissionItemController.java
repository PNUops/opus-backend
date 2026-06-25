package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestSubmissionItemMemoCommandService;
import com.opus.opus.modules.contest.application.ContestSubmissionItemMemoQueryService;
import com.opus.opus.modules.contest.application.dto.request.ContestSubmissionItemMemoRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionItemMemoResponse;
import com.opus.opus.modules.member.domain.Member;
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
@RequestMapping("/contests/{contestId}/teams/{teamId}/submission-items/{submissionItemId}/memos")
@Secured({"ROLE_학생", "ROLE_관리자", "ROLE_교수", "ROLE_직원", "ROLE_외부멘토"})
public class ContestSubmissionItemController {

    private final ContestSubmissionItemMemoCommandService memoCommandService;
    private final ContestSubmissionItemMemoQueryService memoQueryService;

    @PostMapping
    public ResponseEntity<Void> createMemo(@PathVariable final Long contestId, @PathVariable final Long teamId,
                                           @PathVariable final Long submissionItemId,
                                           @Valid @RequestBody final ContestSubmissionItemMemoRequest request,
                                           @LoginMember final Member member
    ) {
        memoCommandService.createMemo(contestId, teamId, submissionItemId, request, member);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<ContestSubmissionItemMemoResponse> getMemo(@PathVariable final Long contestId,
                                                                     @PathVariable final Long teamId,
                                                                     @PathVariable final Long submissionItemId,
                                                                     @LoginMember final Member member
    ) {
        ContestSubmissionItemMemoResponse response = memoQueryService.getMemo(contestId, teamId, submissionItemId,
                member);
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<Void> updateMemo(@PathVariable final Long contestId, @PathVariable final Long teamId,
                                           @PathVariable final Long submissionItemId,
                                           @Valid @RequestBody final ContestSubmissionItemMemoRequest request,
                                           @LoginMember final Member member
    ) {
        memoCommandService.updateMemo(contestId, teamId, submissionItemId, request, member);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMemo(@PathVariable final Long contestId, @PathVariable final Long teamId,
                                           @PathVariable final Long submissionItemId, @LoginMember final Member member
    ) {
        memoCommandService.deleteMemo(contestId, teamId, submissionItemId, member);
        return ResponseEntity.noContent().build();
    }
}
