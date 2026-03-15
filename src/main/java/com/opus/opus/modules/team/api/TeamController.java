package com.opus.opus.modules.team.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.TeamCommandService;
import com.opus.opus.modules.team.application.TeamQueryService;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.application.dto.request.PreviewDeleteRequest;
import com.opus.opus.modules.team.application.dto.request.TeamCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamLikeToggleRequest;
import com.opus.opus.modules.team.application.dto.request.TeamUpdateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamVoteToggleRequest;
import com.opus.opus.modules.team.application.dto.response.TeamCreateResponse;
import com.opus.opus.modules.team.application.dto.response.TeamLikeToggleResponse;
import com.opus.opus.modules.team.application.dto.response.TeamVoteToggleResponse;
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

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @PatchMapping("/{teamId}")
    public ResponseEntity<Void> updateTeam(@PathVariable final Long teamId,
                                           @RequestBody final TeamUpdateRequest request,
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

    @GetMapping("/{teamId}/image/{imageId}")
    public ResponseEntity<Resource> getPreviewImage(@PathVariable final Long teamId, @PathVariable final Long imageId) {
        final ImageResponse imageResponse = teamQueryService.getPreviewImage(teamId, imageId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @PostMapping("/{teamId}/image")
    public ResponseEntity<Void> savePreviewImage(@PathVariable final Long teamId,
                                                 @RequestPart("images") final List<MultipartFile> images) {
        teamCommandService.savePreviewImages(teamId, images);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @DeleteMapping("/{teamId}/image")
    public ResponseEntity<Void> deletePreviewImage(@PathVariable final Long teamId,
                                                   @RequestBody @Valid final PreviewDeleteRequest previewDeleteRequest) {
        teamCommandService.deletePreviewImages(teamId, previewDeleteRequest.imageIds());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Resource> getThumbnailImage(@PathVariable final Long teamId) {
        final ImageResponse imageResponse = teamQueryService.getThumbnailImage(teamId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @PostMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Void> saveThumbnailImage(@PathVariable final Long teamId,
                                                   @RequestPart("image") final MultipartFile image) {
        teamCommandService.saveThumbnailImage(teamId, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @DeleteMapping("/{teamId}/image/thumbnail")
    public ResponseEntity<Void> deleteThumbnailImage(@PathVariable final Long teamId) {
        teamCommandService.deleteThumbnailImage(teamId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{teamId}/image/posters")
    public ResponseEntity<Resource> getPosterImage(@PathVariable final Long teamId) {
        final ImageResponse imageResponse = teamQueryService.getPosterImage(teamId);

        return ResponseEntity.ok()
                .contentType(imageResponse.getMediaType())
                .body(imageResponse.resource());
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @PostMapping("/{teamId}/image/posters")
    public ResponseEntity<Void> savePosterImage(@PathVariable final Long teamId,
                                                @RequestPart("image") final MultipartFile image) {
        teamCommandService.savePosterImage(teamId, image);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Secured({"ROLE_팀장", "ROLE_관리자", "ROLE_팀원"})
    @DeleteMapping("/{teamId}/image/posters")
    public ResponseEntity<Void> deletePosterImage(@PathVariable final Long teamId) {
        teamCommandService.deletePosterImage(teamId);
        return ResponseEntity.noContent().build();
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PutMapping("/{teamId}/votes")
    public ResponseEntity<TeamVoteToggleResponse> toggleVote(@PathVariable Long teamId,
                                                             @RequestBody @Valid TeamVoteToggleRequest request,
                                                             @LoginMember Member member) {
        TeamVoteToggleResponse response = teamCommandService.toggleVote(member.getId(), teamId, request.isVoted());
        return ResponseEntity.ok(response);
    }

    @Secured({"ROLE_회원", "ROLE_관리자"})
    @PutMapping("/{teamId}/likes")
    public ResponseEntity<TeamLikeToggleResponse> toggleLike(@PathVariable final Long teamId,
                                                             @RequestBody @Valid final TeamLikeToggleRequest request,
                                                             @LoginMember final Member member) {
        final TeamLikeToggleResponse response = teamCommandService.toggleLike(member.getId(), teamId,
                request.isLiked());
        return ResponseEntity.ok(response);
    }
}
