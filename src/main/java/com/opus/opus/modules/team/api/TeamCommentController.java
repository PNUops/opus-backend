package com.opus.opus.modules.team.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.TeamCommentCommandService;
import com.opus.opus.modules.team.application.TeamCommentQueryService;
import com.opus.opus.modules.team.application.dto.request.TeamCommentCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamCommentUpdateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCommentResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/comments")
@Secured({"ROLE_학생", "ROLE_관리자"})
public class TeamCommentController {

    private final TeamCommentCommandService teamCommentCommandService;
    private final TeamCommentQueryService teamCommentQueryService;

    @PostMapping
    public ResponseEntity<Void> createTeamComment(
            @PathVariable final Long teamId,
            @Valid @RequestBody final TeamCommentCreateRequest request,
            @LoginMember final Member member
    ) {
        teamCommentCommandService.createComment(teamId, member.getId(), request.description());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<TeamCommentResponse>> getTeamComments(
            @PathVariable final Long teamId
    ) {
        List<TeamCommentResponse> response = teamCommentQueryService.getComments(teamId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<Void> updateTeamComment(
            @PathVariable final Long teamId,
            @PathVariable final Long commentId,
            @Valid @RequestBody final TeamCommentUpdateRequest request,
            @LoginMember final Member member
    ) {
        teamCommentCommandService.updateComment(teamId, commentId, member.getId(), request.description());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteTeamComment(
            @PathVariable final Long teamId,
            @PathVariable final Long commentId,
            @LoginMember final Member member
    ) {
        teamCommentCommandService.deleteComment(teamId, commentId, member.getId());
        return ResponseEntity.noContent().build();
    }
}
