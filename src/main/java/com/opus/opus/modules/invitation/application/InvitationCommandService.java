package com.opus.opus.modules.invitation.application;

import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.ALREADY_ACCEPTED_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.ALREADY_INVITED;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.CANCELED_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.CANNOT_ACCEPT_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.EXPIRED_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.INVALID_INVITATION_ROLE;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.INVITATION_EMAIL_MISMATCH;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.NOT_FOUND_INVITATION;
import static com.opus.opus.modules.invitation.exception.InvitationExceptionType.REPLACED_INVITATION;

import com.opus.opus.global.security.JwtProvider;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestMemberConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.invitation.application.convenience.InvitationConvenience;
import com.opus.opus.modules.invitation.application.dto.request.InvitationCreateRequest;
import com.opus.opus.modules.invitation.application.dto.response.InvitationCreateResponse;
import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationStatus;
import com.opus.opus.modules.invitation.domain.InvitationToken;
import com.opus.opus.modules.invitation.domain.dao.InvitationRepository;
import com.opus.opus.modules.invitation.domain.dao.InvitationTokenRepository;
import com.opus.opus.modules.invitation.exception.InvitationException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.application.dto.response.SignInResponse;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.MemberRoleType;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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

    private final InvitationConvenience invitationConvenience;
    private final ContestConvenience contestConvenience;
    private final ContestMemberConvenience contestMemberConvenience;
    private final MemberConvenience memberConvenience;

    private final InvitationTokenProvider invitationTokenProvider;
    private final JwtProvider jwtProvider;

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

    public SignInResponse acceptInvitation(final Long memberId, final String token) {
        final InvitationToken invitationToken = invitationConvenience.getValidateExistToken(token);
        final Invitation invitation = invitationRepository.findByIdForUpdate(invitationToken.getInvitation().getId())
                .orElseThrow(() -> new InvitationException(NOT_FOUND_INVITATION));
        final Member member = memberConvenience.getValidateExistMemberForUpdate(memberId);

        if (invitation.isAcceptedBy(memberId)) {
            return createSignInResponse(member);
        }
        final LocalDateTime now = LocalDateTime.now();
        validateAcceptable(invitation, invitationToken, member, now);
        final Contest contest = contestConvenience.getValidateExistContest(invitation.getContestId());

        member.changeBaseRoles(resolveAcceptedRoles(member, invitation));
        contestMemberConvenience.createIfAbsent(contest, memberId);
        invitation.accept(memberId, now);

        return createSignInResponse(member);
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

    private void validateAcceptable(final Invitation invitation, final InvitationToken invitationToken,
                                    final Member member, final LocalDateTime now) {
        if (invitation.isCanceled()) {
            throw new InvitationException(CANCELED_INVITATION);
        }
        if (invitation.isAccepted()) {
            throw new InvitationException(ALREADY_ACCEPTED_INVITATION);
        }
        if (!invitationConvenience.isLatestTokenForUpdate(invitationToken)) {
            throw new InvitationException(REPLACED_INVITATION);
        }
        if (invitationToken.isExpired(now)) {
            throw new InvitationException(EXPIRED_INVITATION);
        }
        if (!invitation.getEmail().equals(normalizeEmail(member.getEmail()))) {
            throw new InvitationException(INVITATION_EMAIL_MISMATCH);
        }
        if (member.getRoles().contains(MemberRoleType.ROLE_직원)) {
            throw new InvitationException(CANNOT_ACCEPT_INVITATION);
        }
    }

    private Set<MemberRoleType> resolveAcceptedRoles(final Member member, final Invitation invitation) {
        final Set<MemberRoleType> roles = new HashSet<>(member.getBaseRoles());
        roles.add(invitation.getRole());
        if (member.getCreatedAt().isAfter(invitation.getCreatedAt())) {
            roles.remove(MemberRoleType.ROLE_학생);
        }
        return roles;
    }

    private SignInResponse createSignInResponse(final Member member) {
        final List<String> roles = member.getRoles().stream()
                .map(MemberRoleType::toString)
                .toList();
        final String token = jwtProvider.createToken(String.valueOf(member.getId()), roles, member.getName());
        return SignInResponse.from(member, token);
    }

    private String normalizeEmail(final String email) {
        return email.toLowerCase(Locale.ROOT);
    }
}
