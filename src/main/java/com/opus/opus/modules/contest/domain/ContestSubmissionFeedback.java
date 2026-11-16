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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contest_submission_feedback", uniqueConstraints = {
        @UniqueConstraint(name = "uk_feedback_member_submission_item_team",
                columnNames = {"member_id", "contest_submission_item_id", "team_id"})
})
public class ContestSubmissionFeedback extends BaseEntity {

    private static final int MAX_DESCRIPTION_LENGTH = 3000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = MAX_DESCRIPTION_LENGTH)
    private String description;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Boolean isRead;

    @Column(nullable = false)
    private Long teamId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_submission_item_id", nullable = false)
    private ContestSubmissionItem submissionItem;

    @Builder
    private ContestSubmissionFeedback(final String description, final Long memberId, final Long teamId,
                                      final ContestSubmissionItem submissionItem) {
        this.description = description;
        this.memberId = memberId;
        this.teamId = teamId;
        this.submissionItem = submissionItem;
        this.isRead = false;
    }

    public void updateDescription(final String newDescription) {
        this.description = newDescription;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
