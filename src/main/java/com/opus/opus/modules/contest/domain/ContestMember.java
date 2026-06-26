package com.opus.opus.modules.contest.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@SQLDelete(sql = "UPDATE contest_member SET is_deleted = true WHERE id = ?")
public class ContestMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @Column(nullable = false)
    private Long memberId;

    @ElementCollection
    @CollectionTable(
            name = "contest_member_team_ids",
            joinColumns = @JoinColumn(name = "contest_member_id"))
    @Column(name = "team_id", nullable = false)
    private List<Long> teamIds = new ArrayList<>();

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private ContestMember(final Contest contest, final Long memberId, final List<Long> teamIds) {
        this.contest = contest;
        this.memberId = memberId;
        this.teamIds = teamIds != null ? teamIds : new ArrayList<>();
        this.isDeleted = false;
    }

}
