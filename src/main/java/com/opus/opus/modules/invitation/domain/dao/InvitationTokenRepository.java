package com.opus.opus.modules.invitation.domain.dao;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.opus.opus.modules.invitation.domain.InvitationToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitationTokenRepository extends JpaRepository<InvitationToken, Long> {

    Optional<InvitationToken> findByTokenHash(final String tokenHash);

    Optional<InvitationToken> findTopByInvitationIdOrderByIdDesc(final Long invitationId);

    @Lock(PESSIMISTIC_WRITE)
    @Query("select t from InvitationToken t where t.invitation.id = :invitationId order by t.id desc limit 1")
    Optional<InvitationToken> findLatestByInvitationIdForUpdate(@Param("invitationId") final Long invitationId);
}
