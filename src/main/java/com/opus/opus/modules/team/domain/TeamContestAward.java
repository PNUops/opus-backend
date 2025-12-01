package com.opus.opus.modules.team.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.*;
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
@SQLDelete(sql = "UPDATE team_contest_award SET is_deleted = true WHERE id = ?")
public class TeamContestAward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "contest_award_id", nullable = false)
    private Long contestAwardId;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private TeamContestAward(Team team, Long contestAwardId) {
        this.team = team;
        this.contestAwardId = contestAwardId;
        this.isDeleted = false;
    }
}
