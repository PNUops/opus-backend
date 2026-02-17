package com.opus.opus.modules.contest.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
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
@SQLDelete(sql = "UPDATE contest_team_template SET is_deleted = true WHERE id = ?")
public class ContestTeamTemplate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "contest_id", nullable = false, unique = true)
    private Contest contest;

    @Column(name = "division_required", nullable = false)
    private Boolean divisionRequired;

    @Column(name = "project_name_required", nullable = false)
    private Boolean projectNameRequired;

    @Column(name = "team_name_required", nullable = false)
    private Boolean teamNameRequired;

    @Column(name = "leader_required", nullable = false)
    private Boolean leaderRequired;

    @Column(name = "team_members_required", nullable = false)
    private Boolean teamMembersRequired;

    @Column(name = "professor_required", nullable = false)
    private Boolean professorRequired;

    @Column(name = "github_path_required", nullable = false)
    private Boolean githubPathRequired;

    @Column(name = "youtube_path_required", nullable = false)
    private Boolean youtubePathRequired;

    @Column(name = "production_path_required", nullable = false)
    private Boolean productionPathRequired;

    @Column(name = "overview_required", nullable = false)
    private Boolean overviewRequired;

    @Column(name = "poster_required", nullable = false)
    private Boolean posterRequired;

    @Column(name = "images_required", nullable = false)
    private Boolean imagesRequired;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private ContestTeamTemplate(
            final Contest contest,
            final Boolean divisionRequired,
            final Boolean projectNameRequired,
            final Boolean teamNameRequired,
            final Boolean leaderRequired,
            final Boolean teamMembersRequired,
            final Boolean professorRequired,
            final Boolean githubPathRequired,
            final Boolean youtubePathRequired,
            final Boolean productionPathRequired,
            final Boolean overviewRequired,
            final Boolean posterRequired,
            final Boolean imagesRequired) {
        this.contest = contest;
        this.divisionRequired = divisionRequired;
        this.projectNameRequired = projectNameRequired;
        this.teamNameRequired = teamNameRequired;
        this.leaderRequired = leaderRequired;
        this.teamMembersRequired = teamMembersRequired;
        this.professorRequired = professorRequired;
        this.githubPathRequired = githubPathRequired;
        this.youtubePathRequired = youtubePathRequired;
        this.productionPathRequired = productionPathRequired;
        this.overviewRequired = overviewRequired;
        this.posterRequired = posterRequired;
        this.imagesRequired = imagesRequired;
        this.isDeleted = false;
    }

    public void updateTemplate(
            final Boolean divisionRequired,
            final Boolean projectNameRequired,
            final Boolean teamNameRequired,
            final Boolean leaderRequired,
            final Boolean teamMembersRequired,
            final Boolean professorRequired,
            final Boolean githubPathRequired,
            final Boolean youtubePathRequired,
            final Boolean productionPathRequired,
            final Boolean overviewRequired,
            final Boolean posterRequired,
            final Boolean imagesRequired) {
        this.divisionRequired = divisionRequired;
        this.projectNameRequired = projectNameRequired;
        this.teamNameRequired = teamNameRequired;
        this.leaderRequired = leaderRequired;
        this.teamMembersRequired = teamMembersRequired;
        this.professorRequired = professorRequired;
        this.githubPathRequired = githubPathRequired;
        this.youtubePathRequired = youtubePathRequired;
        this.productionPathRequired = productionPathRequired;
        this.overviewRequired = overviewRequired;
        this.posterRequired = posterRequired;
        this.imagesRequired = imagesRequired;
    }
}
