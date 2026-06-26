package com.opus.opus.modules.contest.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class ContestSubmissionMemo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "contest_submission_id", nullable = false, unique = true)
    private ContestSubmission submission;

    @Builder
    private ContestSubmissionMemo(final String content, final ContestSubmission submission) {
        this.content = content;
        this.submission = submission;
    }

    public void updateContent(final String content) {
        this.content = content;
    }
}
