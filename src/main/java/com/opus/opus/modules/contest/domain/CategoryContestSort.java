package com.opus.opus.modules.contest.domain;

import static com.opus.opus.modules.contest.domain.SidebarSortType.ASC;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryContestSort extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SidebarSortType mode;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "category_id", nullable = false, unique = true)
    private ContestCategory category;

    @Builder
    private CategoryContestSort(final ContestCategory category) {
        this.mode = ASC;
        this.category = category;
    }

    public void updateMode(final SidebarSortType mode) {
        this.mode = mode;
    }
}
