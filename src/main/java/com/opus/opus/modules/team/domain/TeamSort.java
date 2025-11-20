package com.opus.opus.modules.team.domain;

import static com.opus.opus.modules.team.domain.SortType.RANDOM;
import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TeamSort extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SortType mode;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Builder
    private TeamSort(final Team team) {
        this.mode = RANDOM;
        this.team = team;
    }
}
