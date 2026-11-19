package com.opus.opus.modules.invitation.application;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.ALREADY_INVITED;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.INVALID_INVITATION_ROLE;

import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.invitation.application.dto.request.InvitationCreateRequest;
import com.opus.opus.modules.invitation.application.dto.response.InvitationCreateResponse;
import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationStatus;
import com.opus.opus.modules.invitation.domain.InvitationToken;
import com.opus.opus.modules.invitation.domain.dao.InvitationRepository;
import com.opus.opus.modules.invitation.domain.dao.InvitationTokenRepository;
import com.opus.opus.modules.invitation.exception.InvitationException;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.time.LocalDateTime;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InvitationCommandService {

    private static final long INVITATION_TOKEN_TTL_HOURS = 48L;

    private final InvitationRepository invitationRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final InvitationTokenProvider invitationTokenProvider;
    private final ContestConvenience contestConvenience;

    public InvitationCreateResponse createInvitation(final Long adminId, final InvitationCreateRequest request) {
        validateInvitationRole(request.role());
        contestConvenience.getValidateExistContestForUpdate(request.contestId());

        final String email = normalizeEmail(request.email());
        validateNotInvited(request.contestId(), email);

        final Invitation invitation = invitationRepository.save(Invitation.builder()
                .contestId(request.contestId())
                .name(request.name().trim())
                .email(email)
                .role(request.role())
                .build());

        final String token = invitationTokenProvider.generate();
        final InvitationToken invitationToken = invitationTokenRepository.save(InvitationToken.builder()
                .invitation(invitation)
                .tokenHash(invitationTokenProvider.hash(token))
                .expiresAt(LocalDateTime.now().plusHours(INVITATION_TOKEN_TTL_HOURS))
                .issuedBy(adminId)
                .build());

        return InvitationCreateResponse.from(invitation, invitationToken, token);
    }

    private void validateInvitationRole(final MemberRoleType role) {
        if (role != MemberRoleType.ROLE_외부멘토) {
            throw new InvitationException(INVALID_INVITATION_ROLE);
        }
    }

    private void validateNotInvited(final Long contestId, final String email) {
        if (invitationRepository.existsByContestIdAndEmailAndStatusNot(contestId, email, InvitationStatus.CANCELED)) {
            throw new InvitationException(ALREADY_INVITED);
        }
    }

    private String normalizeEmail(final String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
