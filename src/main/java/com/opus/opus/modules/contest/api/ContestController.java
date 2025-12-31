package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.ContestTeamTemplateCommandService;
import com.opus.opus.modules.contest.application.dto.request.ContestCurrentToggleRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.request.TeamTemplateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentToggleResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contests")
public class ContestController {

    private final ContestCommandService contestCommandService;
    private final ContestQueryService contestQueryService;
    private final ContestTeamTemplateCommandService contestTeamTemplateCommandService;

    @GetMapping("/{contestId}/image/banner")
    public ResponseEntity<Resource> getContestBanner(@PathVariable final Long contestId) {
        final ImageResponse imageResponse = contestQueryService.getContestBanner(contestId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured("ROLE_관리자")
    @PostMapping("/{contestId}/image/banner")
    public ResponseEntity<Void> saveContestBanner(@PathVariable final Long contestId,
                                                  @RequestPart("image") final MultipartFile image) {
        contestCommandService.saveBannerImage(contestId, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/{contestId}/image/banner")
    public ResponseEntity<Void> deleteContestBanner(@PathVariable final Long contestId) {
        contestCommandService.deleteBannerImage(contestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ContestResponse>> getAllContests() {
        final List<ContestResponse> responses = contestQueryService.getAllContests();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Secured("ROLE_관리자")
    public ResponseEntity<ContestResponse> createContest(@Valid @RequestBody final ContestRequest request) {
        ContestResponse response = contestCommandService.createContest(request);
        return ResponseEntity.ok(response);
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
    public ResponseEntity<Void> deleteContest(@PathVariable final Long contestId) {
        contestCommandService.deleteContest(contestId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{contestId}/current")
    @Secured("ROLE_관리자")
    public ResponseEntity<ContestCurrentToggleResponse> toggleCurrent(@PathVariable final Long contestId,
                                                                      @Valid @RequestBody final ContestCurrentToggleRequest request) {
        ContestCurrentToggleResponse response = contestCommandService.toggleCurrent(contestId, request.isCurrent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<List<ContestCurrentResponse>> getCurrentContests() {
        List<ContestCurrentResponse> responses = contestQueryService.getCurrentContests();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{contestId}/team-detail-template")
    @Secured("ROLE_관리자")
    public ResponseEntity<Void> updateTeamDetailTemplate(@PathVariable final Long contestId,
                                                         @Valid @RequestBody final TeamTemplateRequest request) {
        contestTeamTemplateCommandService.updateTemplate(contestId, request);
        return ResponseEntity.noContent().build();
    }
}
