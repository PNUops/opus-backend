package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.dto.request.ContestCurrentToggleRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestVotesLimitRequest;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentToggleResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/contests")
public class ContestController {

    private final ContestCommandService contestCommandService;
    private final ContestQueryService contestQueryService;
    private final TeamQueryService teamQueryService;

    @GetMapping("/{contestId}/image/banner")
    public ResponseEntity<Resource> getContestBanner(@PathVariable final Long contestId) {
        final ImageResponse imageResponse = contestQueryService.getContestBanner(contestId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured("ROLE_관리자")
    @PostMapping("/{contestId}/image/banner")
    public ResponseEntity<Void> saveContestBanner(@PathVariable final Long contestId,
                                                  @RequestPart("image") final MultipartFile image) {
        contestCommandService.saveBannerImage(contestId, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/{contestId}/image/banner")
    public ResponseEntity<Void> deleteContestBanner(@PathVariable final Long contestId) {
        contestCommandService.deleteBannerImage(contestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ContestResponse>> getAllContests() {
        final List<ContestResponse> responses = contestQueryService.getAllContests();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Secured("ROLE_관리자")
    public ResponseEntity<ContestResponse> createContest(@Valid @RequestBody final ContestRequest request) {
        ContestResponse response = contestCommandService.createContest(request);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/{contestId}")
    public ResponseEntity<Void> updateContest(@PathVariable final Long contestId,
                                              @Valid @RequestBody final ContestRequest request) {
        contestCommandService.updateContest(contestId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/{contestId}")
    public ResponseEntity<Void> deleteContest(@PathVariable final Long contestId) {
        contestCommandService.deleteContest(contestId);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/{contestId}/current")
    public ResponseEntity<ContestCurrentToggleResponse> toggleCurrent(@PathVariable final Long contestId,
                                                                      @Valid @RequestBody final ContestCurrentToggleRequest request) {
        ContestCurrentToggleResponse response = contestCommandService.toggleCurrent(contestId, request.isCurrent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<List<ContestCurrentResponse>> getCurrentContests() {
        List<ContestCurrentResponse> responses = contestQueryService.getCurrentContests();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{contestId}/vote")
    public ResponseEntity<VotePeriodResponse> getVotePeriod(@PathVariable final Long contestId) {
        return ResponseEntity.ok(contestQueryService.getVotePeriod(contestId));
    }

    @Secured("ROLE_관리자")
    @PutMapping("/{contestId}/vote")
    public ResponseEntity<Void> updateVotePeriod(@PathVariable final Long contestId,
                                                 @Valid @RequestBody final VoteUpdateRequest voteRequest) {
        contestCommandService.updateVotePeriod(contestId, voteRequest);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/{contestId}/votes")
    public ResponseEntity<Void> updateMaxVotesLimit(@PathVariable final Long contestId,
                                                    @Valid @RequestBody final ContestVotesLimitRequest request) {
        contestCommandService.updateMaxVotesLimit(contestId, request.maxVotesLimit());
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @GetMapping("/{contestId}/votes")
    public ResponseEntity<ContestVotesLimitResponse> getMaxVotesLimit(@PathVariable final Long contestId) {
        final ContestVotesLimitResponse response = contestQueryService.getMaxVotesLimit(contestId);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @PutMapping("/{contestId}/sort")
    public ResponseEntity<Void> updateContestSort(@PathVariable final Long contestId,
                                                  @RequestBody @Valid final ContestSortRequest request) {
        contestCommandService.updateContestSort(contestId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @GetMapping("/{contestId}/sort")
    public ResponseEntity<ContestSortResponse> getContestSort(@PathVariable final Long contestId) {
        return ResponseEntity.ok(contestQueryService.getContestSort(contestId));
    }

    @Secured("ROLE_관리자")
    @PutMapping("/{contestId}/sort/custom")
    public ResponseEntity<Void> updateContestSortCustom(@PathVariable final Long contestId,
                                                        @Valid @RequestBody final List<ContestSortCustomRequest> requests) {
        contestCommandService.updateContestSortCustom(contestId, requests);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @GetMapping("/{contestId}/votes/me")
    public ResponseEntity<MemberVoteCountResponse> getMemberVoteCount(@PathVariable Long contestId,
                                                                      @LoginMember Member member) {
        final MemberVoteCountResponse response = teamQueryService.getMemberVoteCount(member.getId(), contestId);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @GetMapping("/{contestId}/ranking")
    public ResponseEntity<List<ContestRankingResponse>> getTeamRanking(@PathVariable final Long contestId) {
        final List<ContestRankingResponse> responses = teamQueryService.getTeamRanking(contestId);
        return ResponseEntity.ok(responses);
    }

    @Secured("ROLE_관리자")
    @GetMapping("/{contestId}/votes/statistics")
    public ResponseEntity<ContestVoteStatisticsResponse> getVoteStatistics(@PathVariable final Long contestId) {
        final ContestVoteStatisticsResponse response = teamQueryService.getVoteStatistics(contestId);
        return ResponseEntity.ok(response);
    }
}
