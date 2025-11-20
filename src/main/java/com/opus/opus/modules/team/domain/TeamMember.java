package com.opus.opus.modules.team.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE team_member SET is_deleted = true where id = ?")
public class TeamMember extends BaseEntity {

    private static final int MAX_ROLE_NAME_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "team_member_roles", joinColumns = @JoinColumn(name = "team_member_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = MAX_ROLE_NAME_LENGTH)
    private Set<TeamMemberRoleType> roles = new HashSet<>();

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private TeamMember(final Long memberId, final Team team, final Set<TeamMemberRoleType> roles) {
        this.memberId = memberId;
        this.team = team;
        this.roles = roles;
        this.isDeleted = false;
    }
}
