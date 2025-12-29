package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.request.PreviewDeleteRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<Resource> getPreviewImage(@PathVariable final Long teamId, @PathVariable final Long imageId) {
        final ImageResponse imageResponse = teamQueryService.getPreviewImage(teamId, imageId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @PostMapping("/{teamId}/image")
    public ResponseEntity<Void> savePreviewImage(@PathVariable final Long teamId,
                                                 @RequestPart("images") final List<MultipartFile> images) {
        teamCommandService.savePreviewImages(teamId, images);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @DeleteMapping("/{teamId}/image")
    public ResponseEntity<Void> deletePreviewImage(@PathVariable final Long teamId,
                                                   @RequestBody @Valid final PreviewDeleteRequest previewDeleteRequest) {
        teamCommandService.deletePreviewImages(teamId, previewDeleteRequest.imageIds());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Resource> getThumbnailImage(@PathVariable final Long teamId) {
        final ImageResponse imageResponse = teamQueryService.getThumbnailImage(teamId);

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

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @DeleteMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Void> deleteThumbnailImage(@PathVariable final Long teamId) {
        teamCommandService.deleteThumbnailImage(teamId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/contests/{contestId}/bulk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Secured("ROLE_관리자")
    public ResponseEntity<TeamBulkCreateResponse> createTeamsFromExcel(
            @PathVariable final Long contestId,
            @RequestParam("file") final MultipartFile file
    ) {
        TeamBulkCreateResponse response = teamCommandService.createTeamsFromExcel(contestId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
