package com.opus.opus.modules.invitation.application;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.invitation.application.convenience.InvitationConvenience;
import com.opus.opus.modules.invitation.application.dto.response.InvitationResponse;
import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationStatus;
import com.opus.opus.modules.invitation.domain.InvitationToken;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationQueryService {

    private static final String REPLACED_STATUS = "REPLACED";
    private static final String EXPIRED_STATUS = "EXPIRED";

    private final InvitationConvenience invitationConvenience;
    private final ContestConvenience contestConvenience;

    public InvitationResponse getInvitation(final String token) {
        final InvitationToken invitationToken = invitationConvenience.getValidateExistToken(token);
        final Invitation invitation = invitationToken.getInvitation();
        final Contest contest = contestConvenience.getValidateExistContest(invitation.getContestId());
        final LocalDateTime now = LocalDateTime.now();

        final String status = resolveStatus(invitation, invitationToken, now);
        return InvitationResponse.from(contest, invitation, invitationToken, status, now);
    }

    private String resolveStatus(final Invitation invitation, final InvitationToken invitationToken,
                                 final LocalDateTime now) {
        if (invitation.isAccepted() || invitation.isCanceled()) {
            return invitation.getStatus().name();
        }
        if (!invitationConvenience.isLatestToken(invitationToken)) {
            return REPLACED_STATUS;
        }
        if (invitationToken.isExpired(now)) {
            return EXPIRED_STATUS;
        }
        return InvitationStatus.PENDING.name();
    }
}
