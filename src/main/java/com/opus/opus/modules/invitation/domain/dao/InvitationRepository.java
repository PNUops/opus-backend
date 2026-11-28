package com.opus.opus.modules.invitation.domain.dao;

import com.opus.opus.modules.invitation.domain.Invitation;
import com.opus.opus.modules.invitation.domain.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    boolean existsByContestIdAndEmailAndStatusNot(final Long contestId, final String email,
                                                  final InvitationStatus status);
}
