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
        @UniqueConstraint(name = "uk_feedback_member_submission", columnNames = {"member_id", "contest_submission_id"})
})
public class ContestSubmissionFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Boolean isRead;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_submission_id", nullable = false)
    private ContestSubmission submission;

    @Builder
    private ContestSubmissionFeedback(final String description, final Long memberId, final ContestSubmission submission) {
        this.description = description;
        this.memberId = memberId;
        this.submission = submission;
        this.isRead = false;
    }

    public void updateDescription(final String newDescription) {
        this.description = newDescription;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
