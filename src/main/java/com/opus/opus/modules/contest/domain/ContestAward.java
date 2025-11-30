package com.opus.opus.modules.contest.domain;

import com.opus.opus.global.base.BaseEntity;
import com.opus.opus.modules.team.domain.TeamAward;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
@SQLDelete(sql = "UPDATE contest_award SET is_deleted = true WHERE id = ?")
public class ContestAward extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(nullable = false)
    private String awardName;

    @Column(nullable = false)
    private String awardColor;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "contestAward")
    private List<TeamAward> teamAwards = new ArrayList<>();

    @Builder
    private ContestAward(Contest contest, String awardName, String awardColor) {
        this.contest = contest;
        this.awardName = awardName;
        this.awardColor = awardColor;
        this.isDeleted = false;
    }

    public void update(String awardName, String awardColor) {
        this.awardName = awardName;
        this.awardColor = awardColor;
    }
}
