package com.opus.opus.modules.invitation.application.dto.response;

import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationToken;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.time.LocalDateTime;

public record InvitationResponse(

        String contestName,

        MemberRoleType role,

        String email,

        String status,

        LocalDateTime expiresAt,

        LocalDateTime serverTime
) {
    public static InvitationResponse from(final Contest contest, final Invitation invitation,
                                          final InvitationToken invitationToken, final String status,
                                          final LocalDateTime serverTime) {
        return new InvitationResponse(
                contest.getContestName(),
                invitation.getRole(),
                invitation.getEmail(),
                status,
                invitationToken.getExpiresAt(),
                serverTime
        );
    }
}
