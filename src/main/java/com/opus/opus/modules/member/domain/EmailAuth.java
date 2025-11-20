package com.opus.opus.modules.member.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class EmailAuth extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean isCorrected;

    @Builder
    private EmailAuth(final String token, final String email) {
        this.token = token;
        this.email = email;
        this.isCorrected = false;
    }

}

