package com.opus.opus.modules.contest.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
import com.opus.opus.modules.contest.application.dto.request.ContestTemplateRequest;
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
@SQLDelete(sql = "UPDATE contest_template SET is_deleted = true WHERE id = ?")
public class ContestTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(nullable = false)
    private Boolean trackRequired;

    @Column(nullable = false)
    private Boolean projectNameRequired;

    @Column(nullable = false)
    private Boolean teamNameRequired;

    @Column(nullable = false)
    private Boolean leaderRequired;

    @Column(nullable = false)
    private Boolean teamMembersRequired;

    @Column(nullable = false)
    private Boolean professorRequired;

    @Column(nullable = false)
    private Boolean githubPathRequired;

    @Column(nullable = false)
    private Boolean youTubePathRequired;

    @Column(nullable = false)
    private Boolean productionPathRequired;

    @Column(nullable = false)
    private Boolean overviewRequired;

    @Column(nullable = false)
    private Boolean posterRequired;

    @Column(nullable = false)
    private Boolean imagesRequired;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private ContestTemplate(final Contest contest, final ContestTemplateRequest request
    ) {
        this.contest = contest;

        this.trackRequired = request.trackRequired();
        this.projectNameRequired = request.projectNameRequired();
        this.teamNameRequired = request.teamNameRequired();
        this.leaderRequired = request.leaderRequired();
        this.teamMembersRequired = request.teamMembersRequired();
        this.professorRequired = request.professorRequired();
        this.githubPathRequired = request.githubPathRequired();
        this.youTubePathRequired = request.youTubePathRequired();
        this.productionPathRequired = request.productionPathRequired();
        this.overviewRequired = request.overviewRequired();
        this.posterRequired = request.posterRequired();
        this.imagesRequired = request.imagesRequired();

        this.isDeleted = false;
    }

    public void updateTemplate(final ContestTemplateRequest request) {
        this.trackRequired = request.trackRequired();
        this.projectNameRequired = request.projectNameRequired();
        this.teamNameRequired = request.teamNameRequired();
        this.leaderRequired = request.leaderRequired();
        this.teamMembersRequired = request.teamMembersRequired();
        this.professorRequired = request.professorRequired();
        this.githubPathRequired = request.githubPathRequired();
        this.youTubePathRequired = request.youTubePathRequired();
        this.productionPathRequired = request.productionPathRequired();
        this.overviewRequired = request.overviewRequired();
        this.posterRequired = request.posterRequired();
        this.imagesRequired = request.imagesRequired();
    }
}
