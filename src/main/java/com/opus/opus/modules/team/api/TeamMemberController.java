package com.opus.opus.modules.team.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.TeamMemberCommandService;
import com.opus.opus.modules.team.application.dto.request.TeamMemberCreateRequest;
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
@Secured({"ROLE_회원", "ROLE_관리자"})
public class TeamMemberController {

    private final TeamMemberCommandService teamMemberCommandService;

    @PostMapping
    public ResponseEntity<Void> createTeamMember(@PathVariable final Long teamId,
                                                 @Valid @RequestBody final TeamMemberCreateRequest request,
                                                 @LoginMember final Member member) {
        teamMemberCommandService.createTeamMember(member, teamId, request.memberStudentId(), request.memberName(),
                request.roleType());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> deleteTeamMember(@PathVariable final Long teamId,
                                                 @PathVariable final Long memberId,
                                                 @LoginMember final Member member) {
        teamMemberCommandService.deleteTeamMember(member, teamId, memberId);
        return ResponseEntity.noContent().build();
    }
}
