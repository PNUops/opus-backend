package com.opus.opus.modules.file.application.storage;

import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LocalFileStorage implements FileStorage {

    private final Path basePath;

    public LocalFileStorage(@Value("${file.storage.local.base-path}") final String basePath) {
        this.basePath = Paths.get(System.getProperty("user.dir")).resolve(basePath);
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new UncheckedIOException("파일 저장소 기본 경로 초기화 실패: " + this.basePath, e);
        }
    }

    @Override
    public void store(final byte[] content, final String relativePath) {
        final Path fullPath = resolveSafely(relativePath);
        try {
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, content);
        } catch (IOException e) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "로컬 디스크에 파일을 저장하는 중 오류가 발생했습니다.", e);
        }
    }

    @Override
    public byte[] load(final String relativePath) {
        final Path fullPath = resolveSafely(relativePath);
        if (!Files.exists(fullPath)) {
            throw new FileException(FileExceptionType.NOT_EXISTS_PHYSICAL_FILE);
        }
        try {
            return Files.readAllBytes(fullPath);
        } catch (IOException e) {
            throw new FileException(FileExceptionType.NOT_EXISTS_PHYSICAL_FILE, FileExceptionType.NOT_EXISTS_PHYSICAL_FILE.errorMessage(), e);
        }
    }

    // 파일 전체를 메모리에 올리지 않고 스트림으로 연다. (대용량/다수 파일 아카이브를 흘려보낼 때 사용)
    @Override
    public InputStream loadAsStream(final String relativePath) {
        final Path fullPath = resolveSafely(relativePath);
        if (!Files.exists(fullPath)) {
            throw new FileException(FileExceptionType.NOT_EXISTS_PHYSICAL_FILE);
        }
        try {
            return Files.newInputStream(fullPath);
        } catch (IOException e) {
            throw new FileException(FileExceptionType.NOT_EXISTS_PHYSICAL_FILE, FileExceptionType.NOT_EXISTS_PHYSICAL_FILE.errorMessage(), e);
        }
    }

    @Override
    public void delete(final String relativePath) {
        final Path fullPath = resolveSafely(relativePath);
        if (Files.exists(fullPath)) {
            try {
                Files.delete(fullPath);
            } catch (IOException | SecurityException e) {
                throw new FileException(FileExceptionType.DELETE_FAILED, "물리 파일 삭제에 실패했습니다. 경로=" + fullPath, e);
            }
        } else {
            log.error("삭제하려는 물리 파일이 존재하지 않습니다: {}", fullPath);
        }
    }

    @Override
    public boolean exists(final String relativePath) {
        return Files.exists(resolveSafely(relativePath));
    }

    // 파일 경로를 안전하게 해석하여 basePath를 벗어나지 않도록 한다
    private Path resolveSafely(final String relativePath) {
        final Path fullPath = basePath.resolve(relativePath).normalize();
        if (!fullPath.startsWith(basePath)) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "잘못된 파일 경로입니다: " + relativePath);
        }
        return fullPath;
    }
}
