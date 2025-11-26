package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contest")
public class ContestController {

    private final ContestCommandService contestCommandService;
    private final ContestQueryService contestQueryService;

    @GetMapping("/{contestId}/image/banner")
    public ResponseEntity<Resource> findContestBanner(@PathVariable Long contestId) {
        ImageResponse imageResponse = contestQueryService.findContestBanner(contestId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }
}
