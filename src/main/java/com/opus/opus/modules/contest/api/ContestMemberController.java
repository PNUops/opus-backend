package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestMemberCommandService;
import com.opus.opus.modules.contest.application.ContestMemberQueryService;
import com.opus.opus.modules.contest.application.dto.request.StaffBatchAssignRequest;
import com.opus.opus.modules.contest.application.dto.request.StaffTeamUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorContestResponse;
import com.opus.opus.modules.contest.application.dto.response.MentorProjectResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionsResponse;
import com.opus.opus.modules.member.domain.Member;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class ContestMemberController {

    private final ContestMemberQueryService contestMemberQueryService;
    private final ContestMemberCommandService contestMemberCommandService;

    @Secured("ROLE_관리자")
    @GetMapping("/contests/{contestId}/staff")
    public ResponseEntity<List<ContestStaffResponse>> getAssignedStaff(@PathVariable final Long contestId,
                                                                       @RequestParam(required = false) final String memberType,
                                                                       @RequestParam(required = false) final String search) {
        return ResponseEntity.ok(contestMemberQueryService.getAssignedStaff(contestId, memberType, search));
    }

    @Secured("ROLE_관리자")
    @PostMapping("/contests/{contestId}/staff/batch")
    public ResponseEntity<Void> assignStaff(@PathVariable final Long contestId,
                                            @Valid @RequestBody final StaffBatchAssignRequest request) {
        contestMemberCommandService.assignStaff(contestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/contests/{contestId}/staff/{contestMemberId}")
    public ResponseEntity<Void> updateAssignedTeams(@PathVariable final Long contestId,
                                                    @PathVariable final Long contestMemberId,
                                                    @Valid @RequestBody final StaffTeamUpdateRequest request) {
        contestMemberCommandService.updateAssignedTeams(contestId, contestMemberId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/contests/{contestId}/staff/{contestMemberId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable final Long contestId,
                                                 @PathVariable final Long contestMemberId) {
        contestMemberCommandService.deleteAssignment(contestId, contestMemberId);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_외부멘토", "ROLE_교수"})
    @GetMapping("/mentors/me/contests")
    public ResponseEntity<List<MentorContestResponse>> getMentorContests(
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(contestMemberQueryService.getMentorContests(member));
    }

    @Secured({"ROLE_외부멘토", "ROLE_교수"})
    @GetMapping("/mentors/me/contests/{contestId}/teams")
    public ResponseEntity<List<MentorProjectResponse>> getMentorContestTeams(
            @PathVariable final Long contestId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(contestMemberQueryService.getMentorContestTeams(contestId, member));
    }

    @Secured({"ROLE_외부멘토", "ROLE_교수"})
    @GetMapping("/mentors/me/contests/{contestId}/teams/{teamId}/submissions")
    public ResponseEntity<TeamSubmissionsResponse> getTeamSubmissions(
            @PathVariable final Long contestId,
            @PathVariable final Long teamId,
            @LoginMember final Member member
    ) {
        return ResponseEntity.ok(contestMemberQueryService.getTeamSubmissions(contestId, teamId, member));
    }
}
