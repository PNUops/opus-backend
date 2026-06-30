package com.opus.opus.modules.contest.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.contest.application.ContestCommandService;
import com.opus.opus.modules.contest.application.ContestQueryService;
import com.opus.opus.modules.contest.application.ContestSubmissionCommandService;
import com.opus.opus.modules.contest.application.ContestSubmissionQueryService;
import com.opus.opus.modules.contest.application.SubmissionStatus;
import com.opus.opus.modules.contest.application.dto.request.ContestCurrentToggleRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestTemplateRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestVotesLimitRequest;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentToggleResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestRankingResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSortResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionDetailResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionStatusResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestSubmissionSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestTemplateResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteLogResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVoteStatisticsResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestVotesLimitResponse;
import com.opus.opus.modules.contest.application.dto.response.SubmissionCreateResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkUploadResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamDashboardSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSubmissionItemResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamSummaryResponse;
import com.opus.opus.modules.contest.application.dto.response.UpcomingSubmissionResponse;
import com.opus.opus.modules.contest.application.dto.response.VotePeriodResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.response.MemberVoteCountResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequiredArgsConstructor
public class ContestController {

    private final ContestCommandService contestCommandService;
    private final ContestQueryService contestQueryService;
    private final ContestSubmissionCommandService contestSubmissionCommandService;
    private final ContestSubmissionQueryService contestSubmissionQueryService;

    @GetMapping("/contests/{contestId}/image/banner")
    public ResponseEntity<Resource> getContestBanner(@PathVariable final Long contestId) {
        final ImageResponse imageResponse = contestQueryService.getContestBanner(contestId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured("ROLE_관리자")
    @PostMapping("/contests/{contestId}/image/banner")
    public ResponseEntity<Void> saveContestBanner(@PathVariable final Long contestId,
                                                  @RequestPart("image") final MultipartFile image) {
        contestCommandService.saveBannerImage(contestId, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/contests/{contestId}/image/banner")
    public ResponseEntity<Void> deleteContestBanner(@PathVariable final Long contestId) {
        contestCommandService.deleteBannerImage(contestId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contests")
    public ResponseEntity<List<ContestResponse>> getAllContests() {
        final List<ContestResponse> responses = contestQueryService.getAllContests();
        return ResponseEntity.ok(responses);
    }

    @Secured("ROLE_관리자")
    @PostMapping("/contests")
    public ResponseEntity<ContestResponse> createContest(@Valid @RequestBody final ContestRequest request) {
        ContestResponse response = contestCommandService.createContest(request);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/contests/{contestId}")
    public ResponseEntity<Void> updateContest(@PathVariable final Long contestId,
                                              @Valid @RequestBody final ContestRequest request) {
        contestCommandService.updateContest(contestId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/contests/{contestId}")
    public ResponseEntity<Void> deleteContest(@PathVariable final Long contestId) {
        contestCommandService.deleteContest(contestId);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/contests/{contestId}/current")
    public ResponseEntity<ContestCurrentToggleResponse> toggleCurrent(@PathVariable final Long contestId,
                                                                      @Valid @RequestBody final ContestCurrentToggleRequest request) {
        ContestCurrentToggleResponse response = contestCommandService.toggleCurrent(contestId, request.isCurrent());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/contests/current")
    public ResponseEntity<List<ContestCurrentResponse>> getCurrentContests() {
        List<ContestCurrentResponse> responses = contestQueryService.getCurrentContests();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/contests/{contestId}/vote")
    public ResponseEntity<VotePeriodResponse> getVotePeriod(@PathVariable final Long contestId) {
        return ResponseEntity.ok(contestQueryService.getVotePeriod(contestId));
    }

    @Secured("ROLE_관리자")
    @PutMapping("/contests/{contestId}/vote")
    public ResponseEntity<Void> updateVotePeriod(@PathVariable final Long contestId,
                                                 @Valid @RequestBody final VoteUpdateRequest voteRequest) {
        contestCommandService.updateVotePeriod(contestId, voteRequest);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @PatchMapping("/contests/{contestId}/votes")
    public ResponseEntity<Void> updateMaxVotesLimit(@PathVariable final Long contestId,
                                                    @Valid @RequestBody final ContestVotesLimitRequest request) {
        contestCommandService.updateMaxVotesLimit(contestId, request.maxVotesLimit());
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @GetMapping("/contests/{contestId}/votes")
    public ResponseEntity<ContestVotesLimitResponse> getMaxVotesLimit(@PathVariable final Long contestId) {
        final ContestVotesLimitResponse response = contestQueryService.getMaxVotesLimit(contestId);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @PutMapping("/contests/{contestId}/sort")
    public ResponseEntity<Void> updateContestSort(@PathVariable final Long contestId,
                                                  @RequestBody @Valid final ContestSortRequest request) {
        contestCommandService.updateContestSort(contestId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @GetMapping("/contests/{contestId}/sort")
    public ResponseEntity<ContestSortResponse> getContestSort(@PathVariable final Long contestId) {
        return ResponseEntity.ok(contestQueryService.getContestSort(contestId));
    }

    @Secured("ROLE_관리자")
    @PutMapping("/contests/{contestId}/sort/custom")
    public ResponseEntity<Void> updateContestSortCustom(@PathVariable final Long contestId,
                                                        @Valid @RequestBody final List<ContestSortCustomRequest> requests) {
        contestCommandService.updateContestSortCustom(contestId, requests);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_학생", "ROLE_관리자", "ROLE_교수", "ROLE_직원", "ROLE_외부멘토"})
    @GetMapping("/contests/{contestId}/votes/me")
    public ResponseEntity<MemberVoteCountResponse> getMemberVoteCount(@PathVariable Long contestId,
                                                                      @LoginMember Member member) {
        final MemberVoteCountResponse response = contestQueryService.getMemberVoteCount(member.getId(), contestId);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @GetMapping("/contests/{contestId}/vote-log")
    public ResponseEntity<Page<ContestVoteLogResponse>> getContestVoteLog(@PathVariable final Long contestId,
                                                                          @RequestParam(defaultValue = "0") @PositiveOrZero int page,
                                                                          @RequestParam(defaultValue = "20") @PositiveOrZero int size) {
        return ResponseEntity.ok(contestQueryService.getContestVoteLog(contestId, page, size));
    }

    @GetMapping("/contests/{contestId}/ranking")
    public ResponseEntity<List<ContestRankingResponse>> getTeamRanking(@PathVariable final Long contestId) {
        final List<ContestRankingResponse> responses = contestQueryService.getTeamRanking(contestId);
        return ResponseEntity.ok(responses);
    }

    @Secured("ROLE_관리자")
    @GetMapping("/contests/{contestId}/votes/statistics")
    public ResponseEntity<ContestVoteStatisticsResponse> getVoteStatistics(@PathVariable final Long contestId) {
        final ContestVoteStatisticsResponse response = contestQueryService.getVoteStatistics(contestId);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_관리자")
    @GetMapping("/contests/{contestId}/team-detail-submissions")
    public ResponseEntity<List<ContestSubmissionResponse>> getTeamSubmissions(@PathVariable final Long contestId) {
        final List<ContestSubmissionResponse> responses = contestQueryService.getTeamSubmissions(contestId);
        return ResponseEntity.ok(responses);
    }

    @Secured({"ROLE_학생", "ROLE_관리자"})
    @GetMapping("/contests/{contestId}/submissions")
    public ResponseEntity<List<TeamSubmissionItemResponse>> getTeamSubmissionStatuses(
            @PathVariable final Long contestId, @RequestParam final Long teamId,
            @RequestParam(required = false) final SubmissionStatus status, @LoginMember final Member member) {
        final List<TeamSubmissionItemResponse> responses = contestSubmissionQueryService.getTeamSubmissionStatuses(
                contestId, teamId, status, member);
        return ResponseEntity.ok(responses);
    }

    @Secured({"ROLE_학생", "ROLE_관리자"})
    @GetMapping("/contests/{contestId}/submissions/upcoming")
    public ResponseEntity<List<UpcomingSubmissionResponse>> getUpcomingTeamSubmissions(
            @PathVariable final Long contestId, @RequestParam final Long teamId, @LoginMember final Member member) {
        final List<UpcomingSubmissionResponse> responses = contestSubmissionQueryService.getUpcomingTeamSubmissions(
                contestId, teamId, member);
        return ResponseEntity.ok(responses);
    }

    @Secured({"ROLE_학생", "ROLE_관리자", "ROLE_교수", "ROLE_직원", "ROLE_외부멘토"})
    @GetMapping("/contests/{contestId}/submissions/{submissionId}")
    public ResponseEntity<ContestSubmissionDetailResponse> getSubmissionDetail(@PathVariable final Long contestId,
                                                                               @PathVariable final Long submissionId,
                                                                               @LoginMember final Member member) {
        final ContestSubmissionDetailResponse response = contestSubmissionQueryService.getSubmissionDetail(contestId,
                submissionId, member);
        return ResponseEntity.ok(response);
    }

    @Secured({"ROLE_학생", "ROLE_관리자"})
    @PostMapping("/contests/{contestId}/submission-items/{submissionItemId}/submissions")
    public ResponseEntity<SubmissionCreateResponse> createSubmission(@PathVariable final Long contestId,
                                                                     @PathVariable final Long submissionItemId,
                                                                     @RequestParam final Long teamId,
                                                                     @RequestPart("files") final List<MultipartFile> files,
                                                                     @LoginMember final Member member) {
        final SubmissionCreateResponse response = contestSubmissionCommandService.createSubmission(contestId,
                submissionItemId, teamId, files, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Secured({"ROLE_학생", "ROLE_관리자"})
    @PostMapping("/contests/{contestId}/submissions/{submissionId}/files")
    public ResponseEntity<Void> addSubmissionFiles(@PathVariable final Long contestId,
                                                   @PathVariable final Long submissionId,
                                                   @RequestPart("files") final List<MultipartFile> files,
                                                   @LoginMember final Member member) {
        contestSubmissionCommandService.addSubmissionFiles(contestId, submissionId, files, member);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_학생", "ROLE_관리자"})
    @DeleteMapping("/contests/{contestId}/submissions/{submissionId}/files/{fileId}")
    public ResponseEntity<Void> deleteSubmissionFile(@PathVariable final Long contestId,
                                                     @PathVariable final Long submissionId,
                                                     @PathVariable final Long fileId,
                                                     @LoginMember final Member member) {
        contestSubmissionCommandService.deleteSubmissionFile(contestId, submissionId, fileId, member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/contests/{contestId}/template")
    public ResponseEntity<ContestTemplateResponse> getContestTemplate(@PathVariable final Long contestId) {
        final ContestTemplateResponse response = contestQueryService.getContestTemplate(contestId);
        return ResponseEntity.ok(response);
    }


    @Secured("ROLE_관리자")
    @PutMapping("/contests/{contestId}/template")
    public ResponseEntity<Void> updateContestTemplate(@PathVariable final Long contestId,
                                                      @Valid @RequestBody final ContestTemplateRequest request) {
        contestCommandService.updateContestTemplate(contestId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_학생", "ROLE_관리자", "ROLE_교수", "ROLE_직원", "ROLE_외부멘토"})
    @GetMapping("/contests/{contestId}/teams")
    public ResponseEntity<List<TeamSummaryResponse>> getAllContestTeamSummaries(@PathVariable final Long contestId,
                                                                                @LoginMember final Member member) {
        final List<TeamSummaryResponse> responses = contestQueryService.getContestTeamSummaries(contestId, member);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/contests/{contestId}/teams/public")
    public ResponseEntity<List<TeamSummaryResponse>> getAllContestTeamSummariesPublic(
            @PathVariable final Long contestId) {
        final List<TeamSummaryResponse> responses = contestQueryService.getContestTeamSummariesPublic(contestId);
        return ResponseEntity.ok(responses);
    }

    @Secured("ROLE_관리자")
    @PostMapping("/contests/{contestId}/teams/bulk")
    public ResponseEntity<TeamBulkUploadResponse> bulkUploadTeams(@PathVariable final Long contestId,
                                                                  @RequestPart("file") final MultipartFile file) {
        final TeamBulkUploadResponse response = contestCommandService.bulkUploadTeams(contestId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Secured({"ROLE_학생", "ROLE_관리자"})
    @GetMapping("/contests/{contestId}/teams/{teamId}/summary")
    public ResponseEntity<TeamDashboardSummaryResponse> getTeamDashboardSummary(@PathVariable final Long contestId,
                                                                                @PathVariable final Long teamId,
                                                                                @LoginMember final Member member) {
        return ResponseEntity.ok(contestQueryService.getTeamDashboardSummary(contestId, teamId, member));
    }

    @Secured("ROLE_관리자")
    @GetMapping("/admin/contests/{contestId}/submissions")
    public ResponseEntity<List<ContestSubmissionStatusResponse>> getSubmissionStatuses(
            @PathVariable final Long contestId, @RequestParam(required = false) final Long submissionItemId,
            @RequestParam(required = false) final SubmissionStatus status,
            @RequestParam(required = false) final Long trackId,
            @RequestParam(required = false) final String keyword) {
        final List<ContestSubmissionStatusResponse> responses = contestSubmissionQueryService.getSubmissionStatuses(
                contestId, submissionItemId, status, trackId, keyword);
        return ResponseEntity.ok(responses);
    }

    @Secured("ROLE_관리자")
    @GetMapping("/admin/contests/{contestId}/submissions/summary")
    public ResponseEntity<ContestSubmissionSummaryResponse> getSubmissionSummary(@PathVariable final Long contestId,
                                                                                 @RequestParam(required = false) final Long submissionItemId,
                                                                                 @RequestParam(required = false) final Long trackId) {
        final ContestSubmissionSummaryResponse response = contestSubmissionQueryService.getSubmissionSummary(
                contestId, submissionItemId, trackId);
        return ResponseEntity.ok(response);
    }
}
