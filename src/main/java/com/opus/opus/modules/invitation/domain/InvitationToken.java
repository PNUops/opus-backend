package com.opus.opus.modules.invitation.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvitationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Long issuedBy;

    @Builder
    private InvitationToken(final Invitation invitation, final String tokenHash, final LocalDateTime expiresAt,
                            final Long issuedBy) {
        this.invitation = invitation;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.issuedBy = issuedBy;
    }

    public boolean isExpired(final LocalDateTime now) {
        return !now.isBefore(expiresAt);
    }
}
