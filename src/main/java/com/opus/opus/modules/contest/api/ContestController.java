package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/{contestId}/image/banner")
    public ResponseEntity<Resource> findContestBanner(@PathVariable Long contestId) {
        ImageResponse imageResponse = contestQueryService.findContestBanner(contestId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_관리자"})
    @PostMapping("/{contestId}/image/banner")
    public ResponseEntity<Void> saveContestBanner(@PathVariable Long contestId,
                                                  @RequestPart("image") final MultipartFile image) {
        contestCommandService.saveBannerImage(contestId, image);
        return ResponseEntity.ok().build();
    }

    @Secured({"ROLE_관리자"})
    @DeleteMapping("/{contestId}/image/banner")
    public ResponseEntity<Void> deleteContestBanner(@PathVariable Long contestId) {
        contestCommandService.deleteBannerImage(contestId);
        return ResponseEntity.ok().build();
    }
}
