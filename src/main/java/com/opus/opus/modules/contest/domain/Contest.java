package com.opus.opus.modules.contest.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
@SQLDelete(sql = "UPDATE contest SET is_deleted = true where id = ?")
public class Contest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String contestName;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Boolean isCurrent;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private LocalDateTime voteStartAt;

    @Column(nullable = false)
    private LocalDateTime voteEndAt;

    @Column(nullable = false)
    private Integer maxVotesLimit;

    @OneToMany(mappedBy = "contest")
    private final List<ContestAward> contestAwards = new ArrayList<>();

    @Builder
    private Contest(final String contestName, final Long categoryId) {
        this.contestName = contestName;
        this.categoryId = categoryId;
        this.isCurrent = false;
        this.isDeleted = false;
        this.voteStartAt = LocalDateTime.now();
        this.voteEndAt = LocalDateTime.now();
        this.maxVotesLimit = 0;
    }

    public void updateIsCurrent(final Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public void updateContest(final Long categoryId, final String contestName) {
        this.categoryId = categoryId;
        this.contestName = contestName;
    }

    public void updateMaxVotesLimit(final Integer maxVotesLimit) {
        this.maxVotesLimit = maxVotesLimit;
    }

    public boolean isVotingPeriod() {
        final LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(voteStartAt) && !now.isAfter(voteEndAt);
    }

    public void updateVotePeriod(final LocalDateTime voteStartAt, final LocalDateTime voteEndAt) {
        this.voteStartAt = voteStartAt;
        this.voteEndAt = voteEndAt;
    }
}
