package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.application.TeamQueryService;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {

    private final TeamQueryService teamQueryService;
    private final TeamCommandService teamCommandService;

    @GetMapping("/{teamId}/image/{imageId}")
    public ResponseEntity<Resource> findPreviewImage(@PathVariable Long teamId, @PathVariable Long imageId) {
        Pair<Resource, String> result = teamQueryService.findPreviewImage(teamId, imageId);
        String mimeType = result.b;
        MediaType mediaType = (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(result.a);
    }


}
