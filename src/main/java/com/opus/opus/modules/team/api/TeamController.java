package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
public class TeamController {
    private final TeamCommandService teamCommandService;

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
