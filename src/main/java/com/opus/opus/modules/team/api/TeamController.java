package com.opus.opus.modules.team.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.request.PreviewDeleteRequest;
import com.opus.opus.modules.team.application.dto.request.TeamCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamUpdateRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCreateResponse;
import com.opus.opus.modules.team.application.dto.response.TeamDetailResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteResponse;
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
@RequestMapping("/teams")
public class TeamController {

    private final TeamQueryService teamQueryService;
    private final TeamCommandService teamCommandService;

    @Secured("ROLE_관리자")
    @PostMapping
    public ResponseEntity<TeamCreateResponse> createTeam(@RequestBody @Valid final TeamCreateRequest request) {
        final TeamCreateResponse response = teamCommandService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PatchMapping("/{teamId}")
    public ResponseEntity<Void> updateTeam(@PathVariable final Long teamId,
                                           @RequestBody @Valid final TeamUpdateRequest request,
                                           @LoginMember final Member member) {
        teamCommandService.updateTeam(member, teamId, request);
        return ResponseEntity.noContent().build();
    }

    @Secured("ROLE_관리자")
    @DeleteMapping("/{teamId}")
    public ResponseEntity<Void> deleteTeam(@PathVariable final Long teamId) {
        teamCommandService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDetailResponse> getTeamDetail(@PathVariable final Long teamId,
                                                            @LoginMember final Member member) {
        final TeamDetailResponse response = teamQueryService.getTeamDetail(teamId, member);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}/public")
    public ResponseEntity<TeamDetailResponse> getTeamDetailPublic(@PathVariable final Long teamId) {
        final TeamDetailResponse response = teamQueryService.getTeamDetailPublic(teamId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{teamId}/image/{imageId}")
    public ResponseEntity<Resource> getPreviewImage(@PathVariable final Long teamId, @PathVariable final Long imageId) {
        final ImageResponse imageResponse = teamQueryService.getPreviewImage(teamId, imageId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PostMapping("/{teamId}/image")
    public ResponseEntity<Void> savePreviewImage(@PathVariable final Long teamId,
                                                 @RequestPart("images") final List<MultipartFile> images,
                                                 @LoginMember final Member member) {
        teamCommandService.savePreviewImages(teamId, images, member);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @DeleteMapping("/{teamId}/image")
    public ResponseEntity<Void> deletePreviewImage(@PathVariable final Long teamId,
                                                   @RequestBody @Valid final PreviewDeleteRequest previewDeleteRequest,
                                                   @LoginMember final Member member) {
        teamCommandService.deletePreviewImages(teamId, previewDeleteRequest.imageIds(), member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Resource> getThumbnailImage(@PathVariable final Long teamId) {
        final ImageResponse imageResponse = teamQueryService.getThumbnailImage(teamId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PostMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Void> saveThumbnailImage(@PathVariable final Long teamId,
                                                   @RequestPart("image") final MultipartFile image,
                                                   @LoginMember final Member member) {
        teamCommandService.saveThumbnailImage(teamId, image, member);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @DeleteMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Void> deleteThumbnailImage(@PathVariable final Long teamId,
                                                     @LoginMember final Member member) {
        teamCommandService.deleteThumbnailImage(teamId, member);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/image/posters")
    public ResponseEntity<Resource> getPosterImage(@PathVariable final Long teamId) {
        final ImageResponse imageResponse = teamQueryService.getPosterImage(teamId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PostMapping("/{teamId}/image/posters")
    public ResponseEntity<Void> savePosterImage(@PathVariable final Long teamId,
                                                @RequestPart("image") final MultipartFile image,
                                                @LoginMember final Member member) {
        teamCommandService.savePosterImage(teamId, image, member);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @DeleteMapping("/{teamId}/image/posters")
    public ResponseEntity<Void> deletePosterImage(@PathVariable final Long teamId,
                                                  @LoginMember final Member member) {
        teamCommandService.deletePosterImage(teamId, member);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PutMapping("/{teamId}/likes")
    public ResponseEntity<Void> addLike(@PathVariable final Long teamId,
                                        @LoginMember final Member member) {
        teamCommandService.addTeamLike(member.getId(), teamId);
        return ResponseEntity.ok().build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @DeleteMapping("/{teamId}/likes")
    public ResponseEntity<Void> removeLike(@PathVariable final Long teamId,
                                           @LoginMember final Member member) {
        teamCommandService.removeTeamLike(member.getId(), teamId);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PutMapping("/{teamId}/votes")
    public ResponseEntity<TeamVoteResponse> addVote(@PathVariable final Long teamId,
                                                    @LoginMember final Member member) {
        final TeamVoteResponse response = teamCommandService.addTeamVote(member.getId(), teamId);
        return ResponseEntity.ok(response);
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @DeleteMapping("/{teamId}/votes")
    public ResponseEntity<TeamVoteResponse> removeVote(@PathVariable final Long teamId,
                                                       @LoginMember final Member member) {
        final TeamVoteResponse response = teamCommandService.removeTeamVote(member.getId(), teamId);
        return ResponseEntity.ok(response);
    }
}
