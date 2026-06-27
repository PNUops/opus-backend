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
import java.time.LocalDateTime;
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
@SQLDelete(sql = "UPDATE contest_submission SET is_deleted = true WHERE id = ?")
public class ContestSubmission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long teamId;

    @Column(nullable = false)
    private LocalDateTime firstSubmittedAt;

    @Column(nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_submission_item_id", nullable = false)
    private ContestSubmissionItem submissionItem;

    @Builder
    private ContestSubmission(final Long teamId, final LocalDateTime firstSubmittedAt,
                              final ContestSubmissionItem submissionItem) {
        this.teamId = teamId;
        this.firstSubmittedAt = firstSubmittedAt;
        this.submissionItem = submissionItem;
        this.isDeleted = false;
    }

    public boolean isInContest(final Long contestId) {
        return submissionItem.isInContest(contestId);
    }

    public boolean isLate() {
        return firstSubmittedAt.isAfter(submissionItem.getEndAt());
    }
}
