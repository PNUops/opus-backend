package com.opus.opus.modules.invitation.application.convenience;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.NOT_FOUND_INVITATION;

import com.opus.opus.modules.invitation.application.InvitationTokenProvider;
import com.opus.opus.modules.invitation.domain.InvitationToken;
import com.opus.opus.modules.invitation.domain.dao.InvitationTokenRepository;
import com.opus.opus.modules.invitation.exception.InvitationException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvitationConvenience {

    private final InvitationTokenRepository invitationTokenRepository;

    private final InvitationTokenProvider invitationTokenProvider;

    public InvitationToken getValidateExistToken(final String token) {
        return invitationTokenRepository.findByTokenHash(invitationTokenProvider.hash(token))
                .orElseThrow(() -> new InvitationException(NOT_FOUND_INVITATION));
    }

    public boolean isLatestToken(final InvitationToken invitationToken) {
        return invitationTokenRepository.findTopByInvitationIdOrderByIdDesc(invitationToken.getInvitation().getId())
                .map(latestToken -> Objects.equals(latestToken.getId(), invitationToken.getId()))
                .orElse(false);
    }
}
