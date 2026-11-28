package com.opus.opus.modules.invitation.application.dto.response;

import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationToken;
import java.time.LocalDateTime;

public record InvitationCreateResponse(

        Long invitationId,

        String token,

        LocalDateTime expiresAt
) {
    public static InvitationCreateResponse from(final Invitation invitation, final InvitationToken invitationToken,
                                                final String token) {
        return new InvitationCreateResponse(invitation.getId(), token, invitationToken.getExpiresAt());
    }
}
