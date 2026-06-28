package com.opus.opus.modules.file.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "file_feedback")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileFeedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "file_id", nullable = false, unique = true)
    private File file;

    @Column(nullable = false)
    private Long feedbackId;

    @Column(nullable = false)
    private Integer fileOrder;

    @Builder
    private FileFeedback(final File file, final Long feedbackId, final Integer fileOrder) {
        this.file = file;
        this.feedbackId = feedbackId;
        this.fileOrder = fileOrder;
    }

    public String getFileName() {
        return file.getName();
    }

    public Long getFileSize() {
        return file.getFileSize();
    }

    public String getFilePath() {
        return file.getFilePath();
    }

    public String getMimeType() {
        return file.getMimeType();
    }
}
