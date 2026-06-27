package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestMemberCommandService;
import com.opus.opus.modules.contest.application.ContestMemberQueryService;
import com.opus.opus.modules.contest.application.dto.request.StaffBatchAssignRequest;
import com.opus.opus.modules.contest.application.dto.request.StaffTeamUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}/staff")
public class ContestMemberController {

    private final ContestMemberQueryService contestMemberQueryService;
    private final ContestMemberCommandService contestMemberCommandService;

    @Secured("ROLE_관리자")
    @GetMapping
    public ResponseEntity<List<ContestStaffResponse>> getAssignedStaff(@PathVariable final Long contestId,
                                                                       @RequestParam(required = false) final String memberType,
                                                                       @RequestParam(required = false) final String search) {
        return ResponseEntity.ok(contestMemberQueryService.getAssignedStaff(contestId, memberType, search));
    }

    @Secured("ROLE_관리자")
    @PostMapping("/batch")
    public ResponseEntity<Void> assignStaff(@PathVariable final Long contestId,
                                            @Valid @RequestBody final StaffBatchAssignRequest request) {
        contestMemberCommandService.assignStaff(contestId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/{contestMemberId}")
    public ResponseEntity<Void> updateAssignedTeams(@PathVariable final Long contestId,
                                                    @PathVariable final Long contestMemberId,
                                                    @Valid @RequestBody final StaffTeamUpdateRequest request) {
        contestMemberCommandService.updateAssignedTeams(contestId, contestMemberId, request);
        return ResponseEntity.noContent().build();
    }
}
