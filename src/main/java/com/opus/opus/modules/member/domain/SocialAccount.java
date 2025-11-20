package com.opus.opus.modules.member.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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
@SQLDelete(sql = "UPDATE social_account SET is_deleted = true where id = ?")
public class SocialAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String socialEmail;

    @Column(nullable = false)
    private Boolean isDeleted;

    @OneToOne
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Builder
    private SocialAccount(final String socialEmail, final Member member) {
        this.socialEmail = socialEmail;
        this.isDeleted = false;
        this.member = member;
    }
}
