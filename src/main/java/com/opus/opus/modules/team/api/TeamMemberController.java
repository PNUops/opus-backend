package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamMemberCommandService;
import com.opus.opus.modules.team.application.dto.request.TeamMemberAddRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/members")
public class TeamMemberController {

    private final TeamMemberCommandService teamMemberCommandService;

    @Secured("ROLE_관리자")
    @PostMapping
    public ResponseEntity<Void> addTeamMember(@PathVariable final Long teamId,
                                              @Valid @RequestBody final TeamMemberAddRequest request) {
        teamMemberCommandService.addTeamMember(teamId, request.teamMemberStudentId(), request.teamMemberName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
