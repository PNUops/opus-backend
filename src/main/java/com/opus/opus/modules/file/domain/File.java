package com.opus.opus.modules.file.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false) // todo: default 경로 고려 필요
    private String filePath;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private ReferenceDomainType referenceType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileImageType imageType;

    @Column(nullable = false)
    private Boolean isWebpConverted;

    @Builder
    private File(final String name, final String filePath, final Long referenceId, final ReferenceDomainType referenceType,
                 final FileImageType imageType) {
        this.name = name;
        this.filePath = filePath;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.imageType = imageType;
        this.isWebpConverted = false;
    }

    public void updateIsWebpConverted(boolean isWebpConverted) {
        this.isWebpConverted = isWebpConverted;
    }

}
