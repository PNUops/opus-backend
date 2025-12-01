package com.opus.opus.modules.team.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.*;
import com.opus.opus.modules.contest.domain.ContestAward;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamAward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "contest_award_id", nullable = false)
    private Long contestAwardId;

    @Builder
    private TeamAward(Team team, Long contestAwardId) {
        this.team = team;
        this.contestAward = contestAward;
        this.contestAwardId = contestAwardId;
        this.isDeleted = false;
    }
}
