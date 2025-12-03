package com.opus.opus.modules.team.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Secured("ROLE_관리자")
public class TeamAdminController {
    @GetMapping("/dashboard")
    public ResponseEntity<List<TeamSubmissionStatusResponse>> getAllTeamSubmissions() {
        return ResponseEntity.ok(teamAdminQueryService.getAllTeamSubmissions());
    }
}
