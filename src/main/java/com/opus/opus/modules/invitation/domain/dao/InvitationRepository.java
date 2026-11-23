package com.opus.opus.modules.invitation.domain.dao;

import static jakarta.persistence.LockModeType.PESSIMISTIC_WRITE;

import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    boolean existsByContestIdAndEmailAndStatusNot(final Long contestId, final String email,
                                                  final InvitationStatus status);

    @Lock(PESSIMISTIC_WRITE)
    @Query("select i from Invitation i where i.id = :invitationId")
    Optional<Invitation> findByIdForUpdate(@Param("invitationId") final Long invitationId);
}
