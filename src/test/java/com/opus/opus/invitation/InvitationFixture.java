package com.opus.opus.invitation;

import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationToken;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.time.LocalDateTime;

public class InvitationFixture {

    public static Invitation createInvitation(final Long contestId, final String email) {
        return Invitation.builder()
                .contestId(contestId)
                .name("외부멘토")
                .email(email)
                .role(MemberRoleType.ROLE_외부멘토)
                .build();
    }

    public static InvitationToken createInvitationToken(final Invitation invitation, final String tokenHash,
                                                        final LocalDateTime expiresAt) {
        return InvitationToken.builder()
                .invitation(invitation)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .issuedBy(1L)
                .build();
    }
}
