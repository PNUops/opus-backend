package com.opus.opus.modules.contest.domain;

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

    @Column(name = "division", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType division;

    @Column(name = "project_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType projectName;

    @Column(name = "team_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType teamName;

    @Column(name = "leader", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType leader;

    @Column(name = "team_members", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType teamMembers;

    @Column(name = "professor", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType professor;

    @Column(name = "github_path", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType githubPath;

    @Column(name = "youtube_path", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType youtubePath;

    @Column(name = "production_path", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType productionPath;

    @Column(name = "overview", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType overview;

    @Column(name = "poster", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType poster;

    @Column(name = "images", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContestTeamTemplateFieldType images;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private ContestTeamTemplate(
            final Contest contest,
            final ContestTeamTemplateFieldType division,
            final ContestTeamTemplateFieldType projectName,
            final ContestTeamTemplateFieldType teamName,
            final ContestTeamTemplateFieldType leader,
            final ContestTeamTemplateFieldType teamMembers,
            final ContestTeamTemplateFieldType professor,
            final ContestTeamTemplateFieldType githubPath,
            final ContestTeamTemplateFieldType youtubePath,
            final ContestTeamTemplateFieldType productionPath,
            final ContestTeamTemplateFieldType overview,
            final ContestTeamTemplateFieldType poster,
            final ContestTeamTemplateFieldType images) {
        this.contest = contest;
        this.division = division;
        this.projectName = projectName;
        this.teamName = teamName;
        this.leader = leader;
        this.teamMembers = teamMembers;
        this.professor = professor;
        this.githubPath = githubPath;
        this.youtubePath = youtubePath;
        this.productionPath = productionPath;
        this.overview = overview;
        this.poster = poster;
        this.images = images;
        this.isDeleted = false;
    }

    public void updateTemplate(
            final ContestTeamTemplateFieldType division,
            final ContestTeamTemplateFieldType projectName,
            final ContestTeamTemplateFieldType teamName,
            final ContestTeamTemplateFieldType leader,
            final ContestTeamTemplateFieldType teamMembers,
            final ContestTeamTemplateFieldType professor,
            final ContestTeamTemplateFieldType githubPath,
            final ContestTeamTemplateFieldType youtubePath,
            final ContestTeamTemplateFieldType productionPath,
            final ContestTeamTemplateFieldType overview,
            final ContestTeamTemplateFieldType poster,
            final ContestTeamTemplateFieldType images) {
        this.division = division;
        this.projectName = projectName;
        this.teamName = teamName;
        this.leader = leader;
        this.teamMembers = teamMembers;
        this.professor = professor;
        this.githubPath = githubPath;
        this.youtubePath = youtubePath;
        this.productionPath = productionPath;
        this.overview = overview;
        this.poster = poster;
        this.images = images;
    }
}
