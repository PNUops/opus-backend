package com.opus.opus.modules.contest.api;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contest/{contestId}/tracks")
public class ContestTrackController {

    private final ContestTrackCommandService contestTrackCommandService;
    private final ContestTrackQueryService contestTrackQueryService;

    @PostMapping
    @Secured("ROLE_관리자")
    public ResponseEntity<ContestTrackResponse> createContestTrack(
            @Valid @RequestBody final ContestTrackRequest request,
            @PathVariable final Long contestId) {
        ContestTrackResponse response = contestTrackCommandService.createTrack(contestId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{trackId}")
    @Secured("ROLE_관리자")
    public ResponseEntity<Void> updateContestTrack(@Valid @RequestBody final ContestTrackRequest request,
                                                   @PathVariable final Long contestId,
                                                   @PathVariable final Long trackId) {
        contestTrackCommandService.updateTrack(contestId, trackId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{trackId}")
    @Secured("ROLE_관리자")
    public ResponseEntity<Void> deleteContestTrack(@PathVariable final Long contestId,
                                                   @PathVariable final Long trackId) {
        contestTrackCommandService.deleteTrack(contestId, trackId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ContestTrackResponse>> getAllContestTracks(@PathVariable final Long contestId) {
        List<ContestTrackResponse> response = contestTrackQueryService.getAllContestTracks(contestId);
        return ResponseEntity.ok(response);
    }
}
