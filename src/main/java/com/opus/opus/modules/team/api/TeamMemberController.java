package com.opus.opus.modules.team.api;

import com.opus.opus.modules.team.application.TeamMemberCommandService;
import com.opus.opus.modules.team.application.dto.request.TeamMemberAddRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/members")
@Secured("ROLE_관리자")
public class TeamMemberController {

    private final TeamMemberCommandService teamMemberCommandService;

    @PostMapping
    public ResponseEntity<Void> addTeamMember(@PathVariable final Long teamId,
                                              @Valid @RequestBody final TeamMemberAddRequest request) {
        teamMemberCommandService.addTeamMember(teamId, request.memberStudentId(), request.memberName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable final Long teamId,
                                                 @PathVariable final Long memberId) {
        teamMemberCommandService.deleteTeamMember(teamId, memberId);
        return ResponseEntity.noContent().build();
    }
}
