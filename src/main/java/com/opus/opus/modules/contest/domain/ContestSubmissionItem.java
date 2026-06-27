package com.opus.opus.modules.contest.domain;

import static jakarta.persistence.FetchType.LAZY;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
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
@SQLDelete(sql = "UPDATE contest_submission_item SET is_deleted = true WHERE id = ?")
public class ContestSubmissionItem extends BaseEntity {

    private static final long MB_IN_BYTES = 1024L * 1024L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @ElementCollection
    @CollectionTable(
            name = "contest_submission_item_file_formats",
            joinColumns = @JoinColumn(name = "contest_submission_item_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "file_format", nullable = false)
    private Set<SubmissionFileFormat> allowedFileFormats = new HashSet<>();

    @Column(nullable = false)
    private Integer maxFileSizeMb;

    @Column(nullable = false)
    private Integer maxFileCount;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private Boolean allowLateSubmission;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionVisibility visibility;

    @Column(nullable = false)
    private Boolean isDeleted;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_id", nullable = false)
    private Contest contest;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "contest_track_id")
    private ContestTrack contestTrack;

    @Builder
    private ContestSubmissionItem(final String name, final String description,
                                  final Set<SubmissionFileFormat> allowedFileFormats, final Integer maxFileSizeMb,
                                  final Integer maxFileCount, final LocalDateTime startAt, final LocalDateTime endAt,
                                  final Boolean allowLateSubmission, final SubmissionVisibility visibility,
                                  final Contest contest, final ContestTrack contestTrack) {
        this.name = name;
        this.description = description;
        this.allowedFileFormats = allowedFileFormats != null ? allowedFileFormats : new HashSet<>();
        this.maxFileSizeMb = maxFileSizeMb;
        this.maxFileCount = maxFileCount;
        this.startAt = startAt;
        this.endAt = endAt;
        this.allowLateSubmission = allowLateSubmission;
        this.visibility = visibility;
        this.contest = contest;
        this.contestTrack = contestTrack;
        this.isDeleted = false;
    }

    public boolean isInContest(final Long contestId) {
        return contest.getId().equals(contestId);
    }

    public boolean isSubmissionClosed() {
        return LocalDateTime.now().isAfter(endAt) && !allowLateSubmission;
    }

    public boolean isFileCountExceeded(final int totalFileCount) {
        return totalFileCount > maxFileCount;
    }

    public boolean isAllowedFormat(final SubmissionFileFormat format) {
        return allowedFileFormats.contains(format);
    }

    public boolean isFileSizeExceeded(final long sizeBytes) {
        return sizeBytes > maxFileSizeMb * MB_IN_BYTES;
    }
}
