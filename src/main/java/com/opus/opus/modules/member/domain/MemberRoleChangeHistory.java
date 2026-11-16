package com.opus.opus.modules.member.domain;

import static jakarta.persistence.FetchType.EAGER;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.util.HashSet;
import java.util.Set;
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

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "member_role_change_history_before_roles",
            joinColumns = @JoinColumn(name = "history_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<MemberRoleType> beforeRoles = new HashSet<>();

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "member_role_change_history_after_roles",
            joinColumns = @JoinColumn(name = "history_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Set<MemberRoleType> afterRoles = new HashSet<>();

    @Builder
    private MemberRoleChangeHistory(final Long memberId, final Long adminId,
                                    final Set<MemberRoleType> beforeRoles,
                                    final Set<MemberRoleType> afterRoles) {
        this.memberId = memberId;
        this.adminId = adminId;
        this.beforeRoles = new HashSet<>(beforeRoles);
        this.afterRoles = new HashSet<>(afterRoles);
    }
}
