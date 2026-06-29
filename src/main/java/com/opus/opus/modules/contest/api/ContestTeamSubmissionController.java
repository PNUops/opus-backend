package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestSubmissionQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionTimelineResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionSummaryResponse;
import com.opus.opus.modules.member.domain.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}/teams/{teamId}/submissions")
@Secured({"ROLE_학생", "ROLE_관리자", "ROLE_교수", "ROLE_직원", "ROLE_외부멘토"})
public class ContestTeamSubmissionController {

    private final ContestSubmissionQueryService contestSubmissionQueryService;

    @GetMapping("/summary")
    public ResponseEntity<TeamSubmissionSummaryResponse> getSubmissionSummary(
            @PathVariable final Long contestId,
            @PathVariable final Long teamId,
            @LoginMember final Member member) {
        return ResponseEntity.ok(contestSubmissionQueryService.getSubmissionSummary(contestId, teamId, member));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<ContestSubmissionTimelineResponse>> getSubmissionTimeline(
            @PathVariable final Long contestId,
            @PathVariable final Long teamId,
            @LoginMember final Member member) {
        return ResponseEntity.ok(contestSubmissionQueryService.getSubmissionTimeline(contestId, teamId, member));
    }
}
