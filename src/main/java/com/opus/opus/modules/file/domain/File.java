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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String mimeType;

    public static File create(final String name, final String filePath, final String mimeType) {
        final File file = new File();
        file.name = name;
        file.filePath = filePath;
        file.mimeType = mimeType;
        return file;
    }

}
