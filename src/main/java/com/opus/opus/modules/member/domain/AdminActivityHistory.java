package com.opus.opus.modules.member.domain;

import static com.opus.opus.modules.member.domain.AdminActivityTargetType.MEMBER;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_admin_activity_history_created_at_id", columnList = "createdAt, id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminActivityHistory extends BaseEntity {

    private static final String ROLE_PREFIX = "ROLE_";
    private static final String NO_ROLE = "없음";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActivityType type;

    @Column(nullable = false)
    private Long actorId;

    @Column(nullable = false)
    private String actorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminActivityTargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String targetName;

    // null이면 서비스 전역 작업이다.
    private Long contestId;

    @Column(nullable = false)
    private String summary;

    private AdminActivityHistory(final AdminActivityType type, final Member actor, final Member target,
                                 final String summary) {
        this.type = type;
        this.actorId = actor.getId();
        this.actorName = actor.getName();
        this.targetType = MEMBER;
        this.targetId = target.getId();
        this.targetName = target.getName();
        this.summary = summary;
    }

    public static AdminActivityHistory rolesChanged(final Member actor, final Member target,
                                                    final Set<MemberRoleType> beforeRoles,
                                                    final Set<MemberRoleType> afterRoles) {
        final String afterNames = roleNames(afterRoles);
        final String summary = "%s님의 역할이 %s에서 %s%s 변경됨".formatted(
                target.getName(), roleNames(beforeRoles), afterNames, directionParticle(afterNames));
        return new AdminActivityHistory(AdminActivityType.MEMBER_ROLES_CHANGED, actor, target, summary);
    }

    public static AdminActivityHistory adminPermissionChanged(final Member actor, final Member target,
                                                              final boolean granted) {
        if (granted) {
            return new AdminActivityHistory(AdminActivityType.ADMIN_PERMISSION_GRANTED, actor, target,
                    "%s님에게 관리자 권한이 부여됨".formatted(target.getName()));
        }
        return new AdminActivityHistory(AdminActivityType.ADMIN_PERMISSION_REVOKED, actor, target,
                "%s님의 관리자 권한이 회수됨".formatted(target.getName()));
    }

    private static String roleNames(final Set<MemberRoleType> roles) {
        if (roles.isEmpty()) {
            return NO_ROLE;
        }
        return roles.stream()
                .sorted(Comparator.comparingLong(MemberRoleType::getId))
                .map(role -> role.name().substring(ROLE_PREFIX.length()))
                .collect(Collectors.joining(", "));
    }

    // "으로/로": 받침이 없거나 ㄹ 받침이면 "로", 그 외 받침은 "으로"
    private static String directionParticle(final String word) {
        final char last = word.charAt(word.length() - 1);
        if (last < '가' || last > '힣') {
            return "로";
        }
        final int finalConsonant = (last - '가') % 28;
        return finalConsonant == 0 || finalConsonant == 8 ? "로" : "으로";
    }
}
