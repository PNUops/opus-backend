package com.opus.opus.modules.member.domain;

import com.opus.opus.global.base.BaseEntity;
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
public class MemberWithdrawalHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    @Enumerated(EnumType.STRING)
    private MemberWithdrawalReason reason;

    private String detail;

    @Builder
    private MemberWithdrawalHistory(final Long memberId, final MemberWithdrawalReason reason, final String detail) {
        this.memberId = memberId;
        this.reason = reason;
        this.detail = detail;
    }
}
