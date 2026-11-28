package com.opus.opus.modules.invitation.domain.dao;

import com.opus.opus.modules.invitation.domain.InvitationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {

    Optional<InvitationToken> findByTokenHash(final String tokenHash);

    Optional<InvitationToken> findTopByInvitationIdOrderByIdDesc(final Long invitationId);
}
