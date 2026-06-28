package com.opus.opus.modules.file.domain;

import com.opus.opus.global.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File extends BaseEntity {

    private static final String DEFAULT_EXTENSION = "bin";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String mimeType;

    @Column(nullable = false)
    private Long fileSize;

    public void updateMimeType(final String mimeType) {
        this.mimeType = mimeType;
    }

    public static File create(final String name, final String filePath, final String mimeType, final Long fileSize) {
        final File file = new File();
        file.name = (name != null) ? name : "unnamed";
        file.filePath = filePath;
        file.mimeType = mimeType;
        file.fileSize = fileSize;
        return file;
    }

    public static String extractExtension(final String filename) {
        if (filename == null || filename.isBlank()) {
            return DEFAULT_EXTENSION;
        }
        final int lastDot = filename.lastIndexOf('.');
        if (lastDot <= 0 || lastDot >= filename.length() - 1) {
            return DEFAULT_EXTENSION;
        }
        return filename.substring(lastDot + 1);
    }

}
