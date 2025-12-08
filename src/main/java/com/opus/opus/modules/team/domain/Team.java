package com.opus.opus.modules.team.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@SQLDelete(sql = "UPDATE team SET is_deleted = true where id = ?")
public class Team extends BaseEntity {

    private static final int MAX_OVERVIEW_LENGTH = 3000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String teamName;

    @Column
    private String projectName;

    @Column
    private String professorName;

    @Column(length = MAX_OVERVIEW_LENGTH)
    private String overview;

    @Column
    private String githubPath;

    @Column
    private String productionPath;

    @Column
    private String youTubePath;

    @Column(nullable = false)
    private Long contestId;

    @Column
    private Long trackId;

    @Column(nullable = false)
    private Integer itemOrder;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private Boolean isSubmitted;

    @OneToMany(mappedBy = "team")
    private List<TeamMember> teamMembers = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<TeamComment> teamComments = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<TeamContestAward> teamAwards = new ArrayList<>();

    @OneToMany(mappedBy = "team")
    private List<TeamLike> teamLikes = new ArrayList<>();

    @Builder
    private Team(final String teamName, final String projectName, final String professorName, final String overview,
                 final String githubPath, final String productionPath, final String youTubePath, final Long contestId,
                 final Long trackId, final Integer itemOrder, final List<TeamMember> teamMembers) {
        this.teamName = teamName;
        this.projectName = projectName;
        this.professorName = professorName;
        this.overview = overview;
        this.githubPath = githubPath;
        this.productionPath = productionPath;
        this.youTubePath = youTubePath;
        this.contestId = contestId;
        this.trackId = trackId;
        this.itemOrder = itemOrder;
        this.isDeleted = false;
        this.isSubmitted = false;
        this.teamMembers = teamMembers;
    }

}
