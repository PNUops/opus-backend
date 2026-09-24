package com.opus.opus.modules.contest.domain;

import static com.opus.opus.modules.contest.domain.SidebarSortType.ASC;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SidebarCategorySort extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SidebarSortType mode;

    public static SidebarCategorySort createDefault() {
        final SidebarCategorySort sort = new SidebarCategorySort();
        sort.mode = ASC;
        return sort;
    }

    public void updateMode(final SidebarSortType mode) {
        this.mode = mode;
    }
}
