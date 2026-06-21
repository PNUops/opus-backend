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
@SQLDelete(sql = "UPDATE contest_submission_comment SET is_deleted = true WHERE id = ?")
public class ContestSubmissionComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long memberId;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_submission_id", nullable = false)
    private ContestSubmission submission;

    @Column(nullable = false)
    private Boolean isDeleted;

    @Builder
    private ContestSubmissionComment(final String description, final Long memberId, final ContestSubmission submission) {
        this.description = description;
        this.memberId = memberId;
        this.submission = submission;
        this.isDeleted = false;
    }

    public void updateDescription(final String newDescription) {
        this.description = newDescription;
    }

    public boolean isOwner(final Long memberId) {
        return this.memberId.equals(memberId);
    }
}
