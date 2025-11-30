package com.opus.opus.modules.team.domain;

import com.opus.opus.global.base.BaseEntity;
import com.opus.opus.modules.contest.domain.ContestAward;
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
@SQLDelete(sql = "UPDATE team_award SET is_deleted = true WHERE id = ?")
public class TeamAward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_award_id", nullable = false)
    private ContestAward contestAward;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    private TeamAward(Team team, ContestAward contestAward) {
        this.team = team;
        this.contestAward = contestAward;
        this.isDeleted = false;
    }
}
