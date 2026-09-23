package com.opus.opus.modules.team.domain;

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
@SQLDelete(sql = "UPDATE team_comment SET is_deleted = true where id = ?")
public class TeamComment extends BaseEntity {

    private static final int MAX_DESCRIPTION_LENGTH = 3000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_DESCRIPTION_LENGTH)
    private String description;

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TeamCommentVisibility visibility;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private TeamComment(final String description, final Long memberId, final Team team,
                        final TeamCommentVisibility visibility) {
        this.description = description;
        this.memberId = memberId;
        this.team = team;
        this.visibility = visibility != null ? visibility : TeamCommentVisibility.PUBLIC;
        this.isDeleted = false;
    }

    public void updateDescription(final String newDescription) {
        this.description = newDescription;
    }

    public boolean isMine(Long memberId) {
        return this.memberId.equals(memberId);
    }

    public boolean isPublic() {
        return visibility == TeamCommentVisibility.PUBLIC;
    }
}
