package com.opus.opus.modules.team.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.TeamVoteCommandService;
import com.opus.opus.modules.team.application.dto.request.TeamVoteToggleRequest;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams")
@Secured({"ROLE_회원", "ROLE_관리자"})
public class TeamVoteController {

    private final TeamVoteCommandService teamVoteCommandService;

    @PutMapping("/{teamId}/votes")
    public ResponseEntity<TeamVoteToggleResponse> toggleVote(@PathVariable Long teamId,
                                                             @RequestBody @Valid TeamVoteToggleRequest request,
                                                             @LoginMember Member member) {
        TeamVoteToggleResponse response = teamVoteCommandService.toggleVote(member.getId(), teamId, request.isVoted());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/votes")
    public ResponseEntity<MemberVoteCountResponse> getMemberVoteCount(@RequestParam Long contestId,
                                                                      @LoginMember Member member) {
        MemberVoteCountResponse response = teamVoteCommandService.getMemberVoteCount(member.getId(), contestId);
        return ResponseEntity.ok(response);
    }
}
