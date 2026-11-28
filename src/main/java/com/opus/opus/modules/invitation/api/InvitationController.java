package com.opus.opus.modules.invitation.api;

import com.opus.opus.global.security.annotation.LoginMember;
import com.opus.opus.modules.invitation.application.InvitationCommandService;
import com.opus.opus.modules.invitation.application.InvitationQueryService;
import com.opus.opus.modules.invitation.application.dto.request.InvitationCreateRequest;
import com.opus.opus.modules.invitation.application.dto.response.InvitationCreateResponse;
import com.opus.opus.modules.invitation.application.dto.response.InvitationResponse;
import com.opus.opus.modules.member.domain.Member;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationCommandService invitationCommandService;
    private final InvitationQueryService invitationQueryService;

    @Secured("ROLE_관리자")
    @PostMapping("/admin/invitations")
    public ResponseEntity<InvitationCreateResponse> createInvitation(@LoginMember final Member admin,
                                                                     @Valid @RequestBody final InvitationCreateRequest request) {
        final InvitationCreateResponse response = invitationCommandService.createInvitation(admin.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/invitations/{token}")
    public ResponseEntity<InvitationResponse> getInvitation(@PathVariable final String token) {
        return ResponseEntity.ok(invitationQueryService.getInvitation(token));
    }
}
