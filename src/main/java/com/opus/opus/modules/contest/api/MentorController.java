package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.MentorQueryService;
import com.opus.opus.modules.contest.application.dto.response.MentorContestResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
import com.opus.opus.modules.member.domain.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mentors")
@Secured({"ROLE_외부멘토", "ROLE_교수"})
public class MentorController {

    private final MentorQueryService mentorQueryService;

    @GetMapping("/me/contests")
    public ResponseEntity<List<MentorContestResponse>> getMentorContests(
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(mentorQueryService.getMentorContests(member));
    }

    @GetMapping("/me/contests/{contestId}/teams")
    public ResponseEntity<List<MentorProjectResponse>> getMentorContestTeams(
            @PathVariable final Long contestId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(mentorQueryService.getMentorContestTeams(contestId, member));
    }

    @GetMapping("/me/contests/{contestId}/teams/{teamId}/submissions")
    public ResponseEntity<TeamSubmissionsResponse> getTeamSubmissions(
            @PathVariable final Long contestId,
            @PathVariable final Long teamId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(mentorQueryService.getTeamSubmissions(contestId, teamId, member));
    }
}
