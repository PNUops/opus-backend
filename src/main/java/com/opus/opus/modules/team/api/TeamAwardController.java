package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamAwardCommandService;
import com.opus.opus.modules.team.dto.request.TeamAwardUpdateRequest;
import com.opus.opus.modules.team.dto.response.TeamAwardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/teams/{teamId}/awards")
@Secured("ROLE_관리자")
public class TeamAwardController {

    private final TeamAwardCommandService teamAwardCommandService;

    @PutMapping
    public ResponseEntity<TeamAwardResponse> updateTeamAwards(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamAwardUpdateRequest request) {
        TeamAwardResponse response = teamAwardCommandService.updateTeamAwards(teamId, request);
        return ResponseEntity.ok(response);
    }
}
