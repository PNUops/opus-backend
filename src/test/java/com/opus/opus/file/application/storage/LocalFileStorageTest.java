package com.opus.opus.file.application.storage;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.modules.file.application.storage.LocalFileStorage;
import com.opus.opus.modules.file.exception.FileException;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileStorageTest {

    @TempDir
    Path tempDir;

    private LocalFileStorage localFileStorage;

    @BeforeEach
    void setUp() {
        // LocalFileStorage 생성자는 내부에서 user.dir 기준으로 resolve하므로
        // @TempDir의 절대 경로를 그대로 사용하면 이미 절대 경로라 user.dir 기준 resolve가 무효화됨.
        // (Java NIO: absolute.resolve(absolute) → 두 번째 절대 경로 반환)
        localFileStorage = new LocalFileStorage(tempDir.toString());
    }

    @Test
    @DisplayName("[보안] store 시 경로 순회 시도는 예외가 발생한다.")
    void store_경로순회_시도시_예외발생() {
        // Given
        final String traversalPath = "../../../etc/passwd";
        final byte[] content = "malicious data".getBytes();

        // When & Then
        assertThatThrownBy(() -> localFileStorage.store(content, traversalPath))
                .isInstanceOf(FileException.class);
    }

    @Test
    @DisplayName("[보안] load 시 경로 순회 시도는 예외가 발생한다.")
    void load_경로순회_시도시_예외발생() {
        // Given
        final String traversalPath = "../../../etc/passwd";

        // When & Then
        assertThatThrownBy(() -> localFileStorage.load(traversalPath))
                .isInstanceOf(FileException.class);
    }
}
