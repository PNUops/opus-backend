package com.opus.opus.modules.file.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "file_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // cascade=ALL + orphanRemoval=true: FileImage 삭제 시 File도 함께 삭제
    // 파일 모듈은 예외적으로 물리 삭제와 cascade를 허용한다
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "file_id", nullable = false, unique = true)
    private File file;

    @Column(nullable = false)
    private Long referenceId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReferenceDomainType referenceType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileImageType imageType;

    @Column(nullable = false)
    private Boolean isWebpConverted;

    @Builder
    private FileImage(final File file, final Long referenceId, final ReferenceDomainType referenceType,
                      final FileImageType imageType) {
        this.file = file;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.imageType = imageType;
        this.isWebpConverted = false;
    }

    public void markWebpConverted() {
        this.isWebpConverted = true;
    }

    public String getFilePath() {
        return file.getFilePath();
    }

}
