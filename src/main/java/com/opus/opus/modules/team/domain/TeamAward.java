package com.opus.opus.modules.team.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@SQLDelete(sql = "UPDATE team_award SET is_deleted = true where id = ?")
public class TeamAward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String awardName;

    @Column(nullable = false)
    private String awardColor;

    @Column(nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Builder
    private TeamAward(final String awardName, final String awardColor, final Team team) {
        this.awardName = awardName;
        this.awardColor = awardColor;
        this.isDeleted = false;
        this.team = team;
    }
}
