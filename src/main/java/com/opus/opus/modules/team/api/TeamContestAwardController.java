package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamContestAwardCommandService;
import com.opus.opus.modules.team.application.TeamContestAwardQueryService;
import com.opus.opus.modules.team.dto.request.TeamContestAwardUpdateRequest;
import com.opus.opus.modules.team.dto.response.TeamContestAwardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
public class TeamContestAwardController {

    private final TeamContestAwardCommandService teamContestAwardCommandService;
    private final TeamContestAwardQueryService teamContestAwardQueryService;

    @PutMapping
    public ResponseEntity<TeamContestAwardResponse> updateTeamContestAwards(
            @PathVariable Long teamId,
            @Valid @RequestBody TeamContestAwardUpdateRequest request) {
        TeamContestAwardResponse response = teamContestAwardCommandService.updateTeamAwards(teamId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<TeamContestAwardResponse> getTeamContestAwards(@PathVariable Long teamId) {
        TeamContestAwardResponse response = teamContestAwardQueryService.getTeamAwards(teamId);
        return ResponseEntity.ok(response);
    }
}
