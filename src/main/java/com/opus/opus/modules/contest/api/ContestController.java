package com.opus.opus.modules.contest.api;

import static org.springframework.http.HttpStatus.CREATED;

import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController("/contests")
@RequiredArgsConstructor
public class ContestController {
    private final ContestQueryService contestQueryService;
    private final ContestCommandService contestCommandService;

    @GetMapping
    public ResponseEntity<List<ContestResponse>> getAllContests() {
        List<ContestResponse> responses = contestQueryService.getAllContests();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Secured("ROLE_관리자")
    public ResponseEntity<Void> createContest(@Valid @RequestBody final ContestRequest request) {
        contestCommandService.createContest(request);
        return ResponseEntity.status(CREATED).build();
    }

    @PatchMapping("/{contestId}")
    @Secured("ROLE_관리자")
    public ResponseEntity<Void> updateContest(@PathVariable final Long contestId,
                                              @Valid @RequestBody final ContestRequest request) {
        contestCommandService.updateContest(contestId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{contestId}")
    @Secured("ROLE_관리자")
    public ResponseEntity<Void> deleteContest(
            @PathVariable final Long contestId
    ) {
        contestCommandService.deleteContest(contestId);
        return ResponseEntity.noContent().build();
    }
}
