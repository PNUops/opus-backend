package com.opus.opus.modules.contest.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@SQLDelete(sql = "UPDATE contest_submission_memo SET is_deleted = true WHERE id = ?")
public class ContestSubmissionMemo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Column(nullable = false)
    private Long teamId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_submission_item_id", nullable = false)
    private ContestSubmissionItem submissionItem;

    @Builder
    private ContestSubmissionMemo(final String content, final Long teamId,
                                  final ContestSubmissionItem submissionItem) {
        this.content = content;
        this.teamId = teamId;
        this.submissionItem = submissionItem;
        this.isDeleted = false;
    }

    public void updateContent(final String content) {
        this.content = content;
    }
}
