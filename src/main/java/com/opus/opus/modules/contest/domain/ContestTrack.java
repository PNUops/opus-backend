package com.opus.opus.modules.contest.domain;

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
@SQLDelete(sql = "UPDATE contest_track SET is_deleted = true WHERE id = ?")
public class ContestTrack extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String trackName;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private ContestTrack(final String trackName, final Contest contest) {
        this.trackName = trackName;
        this.contest = contest;
        this.isDeleted = false;
    }

    public void updateTrack(final Contest contest, final String trackName) {
        this.contest = contest;
        this.trackName = trackName;
    }
}
