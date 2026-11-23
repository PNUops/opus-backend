package com.opus.opus.modules.invitation.domain;

import com.opus.opus.global.base.BaseEntity;
import com.opus.opus.modules.member.domain.MemberRoleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long contestId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRoleType role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    private Long acceptedMemberId;

    private LocalDateTime acceptedAt;

    private Long canceledBy;

    private LocalDateTime canceledAt;

    @Builder
    private Invitation(final Long contestId, final String name, final String email, final MemberRoleType role) {
        this.contestId = contestId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = InvitationStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }

    public boolean isCanceled() {
        return status == InvitationStatus.CANCELED;
    }

    public boolean isAcceptedBy(final Long memberId) {
        return isAccepted() && Objects.equals(acceptedMemberId, memberId);
    }

    public void accept(final Long memberId, final LocalDateTime now) {
        this.status = InvitationStatus.ACCEPTED;
        this.acceptedMemberId = memberId;
        this.acceptedAt = now;
    }
}
