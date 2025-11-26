package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.request.PreviewDeleteRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {

    private final TeamQueryService teamQueryService;
    private final TeamCommandService teamCommandService;

    @GetMapping("/{teamId}/image/{imageId}")
    public ResponseEntity<Resource> findPreviewImage(@PathVariable Long teamId, @PathVariable Long imageId) {
        ImageResponse imageResponse = teamQueryService.findPreviewImage(teamId, imageId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @PostMapping("/{teamId}/image")
    public ResponseEntity<Void> savePreviewImage(@PathVariable Long teamId,
                                                 @RequestPart("images") final List<MultipartFile> images) {
        teamCommandService.savePreviewImages(teamId, images);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @DeleteMapping("/{teamId}/image")
    public ResponseEntity<Void> deletePreviewImage(@PathVariable Long teamId,
                                                   @RequestBody @Valid PreviewDeleteRequest previewDeleteRequest) {
        teamCommandService.deletePreviewImages(teamId, previewDeleteRequest.imageIds());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Resource> getThumbnailImage(@PathVariable Long teamId) {
        ImageResponse imageResponse = teamQueryService.findThumbnailImage(teamId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @PostMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Void> saveThumbnailImage(@PathVariable final Long teamId,
                                                   @RequestPart("image") final MultipartFile image) {
        teamCommandService.saveThumbnailImage(teamId, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
