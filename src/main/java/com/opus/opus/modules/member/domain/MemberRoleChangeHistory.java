package com.opus.opus.modules.member.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRoleChangeHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRoleType beforeRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRoleType afterRole;

    @Builder
    private MemberRoleChangeHistory(final Long memberId, final Long adminId, final MemberRoleType beforeRole,
                                    final MemberRoleType afterRole) {
        this.memberId = memberId;
        this.adminId = adminId;
        this.beforeRole = beforeRole;
        this.afterRole = afterRole;
    }
}
