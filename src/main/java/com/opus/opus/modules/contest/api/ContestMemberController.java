package com.opus.opus.modules.contest.api;

import com.opus.opus.modules.contest.application.ContestMemberQueryService;
import com.opus.opus.modules.contest.application.dto.response.ContestStaffResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contests/{contestId}/staff")
public class ContestMemberController {

    private final ContestMemberQueryService contestMemberQueryService;

    @Secured("ROLE_관리자")
    @GetMapping
    public ResponseEntity<List<ContestStaffResponse>> getAssignedStaff(@PathVariable final Long contestId,
                                                                       @RequestParam(required = false) final String memberType,
                                                                       @RequestParam(required = false) final String search) {
        return ResponseEntity.ok(contestMemberQueryService.getAssignedStaff(contestId, memberType, search));
    }
}
