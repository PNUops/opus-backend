package com.opus.opus.modules.file.application.storage;

import java.io.InputStream;

public interface FileStorage {

    void store(byte[] content, String relativePath);

    byte[] load(String relativePath);

    InputStream loadAsStream(String relativePath);

    void delete(String relativePath);

    boolean exists(String relativePath);
}
