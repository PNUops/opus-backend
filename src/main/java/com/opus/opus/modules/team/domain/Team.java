package com.opus.opus.modules.team.domain;

import com.opus.opus.global.base.BaseEntity;
import com.opus.opus.modules.team.application.dto.request.TeamCreateRequest;
import com.opus.opus.modules.team.application.dto.request.TeamUpdateRequest;
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
    @OneToMany(mappedBy = "team")
    private final List<TeamComment> teamComments = new ArrayList<>();
    @OneToMany(mappedBy = "team")
    private final List<TeamContestAward> teamAwards = new ArrayList<>();
    @OneToMany(mappedBy = "team")
    private final List<TeamLike> teamLikes = new ArrayList<>();
    @OneToMany(mappedBy = "team")
    private final List<TeamVote> teamVotes = new ArrayList<>();
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

    @Builder
    private Team(final String teamName, final String projectName, final String professorName, final String overview,
                 final String githubPath, final String productionPath, final String youTubePath, final Long contestId,
                 final Long trackId, final Integer itemOrder, final List<TeamMember> teamMembers,
                 final List<TeamContestAward> teamAwards) {
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
        this.teamMembers = teamMembers != null ? teamMembers : new ArrayList<>();
        if (teamAwards != null) {
            this.teamAwards.addAll(teamAwards);
        }
    }

    public static Team from(final TeamCreateRequest request, final int itemOrder) {
        return Team.builder()
                .contestId(request.contestId())
                .trackId(request.trackId())
                .projectName(request.projectName())
                .teamName(request.teamName())
                .professorName(request.professorName())
                .githubPath(request.githubPath())
                .productionPath(request.productionPath())
                .youTubePath(request.youTubePath())
                .overview(request.overview())
                .itemOrder(itemOrder)
                .teamMembers(new ArrayList<>())
                .build();
    }

    public void updateItemOrder(final Integer newOrder) {
        this.itemOrder = newOrder;
    }

    public void update(final TeamUpdateRequest request) {
        if (request.contestId() != null) {
            this.contestId = request.contestId();
        }
        if (request.trackId() != null) {
            this.trackId = request.trackId();
        }
        if (request.teamName() != null) {
            this.teamName = request.teamName();
        }
        if (request.projectName() != null) {
            this.projectName = request.projectName();
        }
        if (request.professorName() != null) {
            this.professorName = request.professorName();
        }
        if (request.overview() != null) {
            this.overview = request.overview();
        }
        if (request.githubPath() != null) {
            this.githubPath = request.githubPath();
        }
        if (request.productionPath() != null) {
            this.productionPath = request.productionPath();
        }
        if (request.youTubePath() != null) {
            this.youTubePath = request.youTubePath();
        }
    }

    public void submit() {
        this.isSubmitted = true;
    }
}
