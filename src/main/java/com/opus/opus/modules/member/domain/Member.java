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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE member SET is_deleted = true where id = ?")
public class Member extends BaseEntity {

    private static final int MAX_ROLE_NAME_LENGTH = 20;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(unique = true)
    private String studentId;

    @Enumerated(EnumType.STRING)
    private SocialType socialType;

    private String socialId;

    @ElementCollection(fetch = EAGER)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = MAX_ROLE_NAME_LENGTH)
    private Set<MemberRoleType> roles = new HashSet<>();

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private Boolean isFake;

    @Builder(builderMethodName = "generalMember", builderClassName = "GeneralMemberBuilder")
    private Member(final String name, final String email, final String password, final String studentId,
                   final Set<MemberRoleType> roles) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.roles = roles;
        this.isDeleted = false;
        this.isFake = false;
    }

    @Builder(builderMethodName = "socialMember", builderClassName = "SocialMemberBuilder")
    private Member(final String name, final String email, final SocialType socialType, final String socialId,
                   final Set<MemberRoleType> roles) {
        this.name = name;
        this.email = email;
        this.socialType = socialType;
        this.socialId = socialId;
        this.roles = roles;
        this.isDeleted = false;
        this.isFake = false;
    }

    public void updateTeamLeaderInfo(final String email, final String password) {
        this.email = email;
        this.password = password;
    }

    public void updatePassword(final String newPassword) {
        this.password = newPassword;
    }

    public boolean isSocialMember() {
        return socialType != null && socialId != null;
    }

    public boolean isPusanEmail() {
        return email != null && email.endsWith("@pusan.ac.kr");
    }

    public void updateStudentId(final String studentId) {
        this.studentId = studentId;
    }

    public boolean isFakeMember() {
        return Boolean.TRUE.equals(isFake);
    }

    public void markAsFakeMember() {
        this.isFake = true;
    }

    public void convertFromFakeToGeneral(final String email, final String password, final String studentId) {
        this.email = email;
        this.password = password;
        this.studentId = studentId;
        this.isFake = false;
    }

    public void convertFromFakeToSocial(final SocialType socialType, final String socialId) {
        this.socialType = socialType;
        this.socialId = socialId;
        this.password = null;
        this.isFake = false;
    }
}
