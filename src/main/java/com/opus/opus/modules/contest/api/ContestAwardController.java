package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestAwardCommandService;
import com.opus.opus.modules.contest.dto.request.ContestAwardRequest;
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
@RequestMapping("/contests/{contestId}/awards")
@Secured("ROLE_관리자")
public class ContestAwardController {
    private final ContestAwardCommandService contestAwardCommandService;

    @PostMapping
    public ResponseEntity<Void> createContestAward(
            @Valid @RequestBody ContestAwardRequest request,
            @PathVariable Long contestId) {
        contestAwardCommandService.createContestAward(contestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
