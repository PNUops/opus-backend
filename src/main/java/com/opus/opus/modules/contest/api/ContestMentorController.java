package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestMentorQueryService;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectsResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
import com.opus.opus.modules.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}")
@Secured({"ROLE_교수", "ROLE_외부멘토"})
public class ContestMentorController {

    private final ContestMentorQueryService contestMentorQueryService;

    @GetMapping("/mentor/projects")
    public ResponseEntity<MentorProjectsResponse> getMentorProjects(
            @PathVariable final Long contestId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(contestMentorQueryService.getMentorProjects(contestId, member));
    }

    @GetMapping("/teams/{teamId}/submissions")
    public ResponseEntity<TeamSubmissionsResponse> getTeamSubmissions(
            @PathVariable final Long contestId,
            @PathVariable final Long teamId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(contestMentorQueryService.getTeamSubmissions(contestId, teamId, member));
    }
}
