package com.opus.opus.modules.file.infrastructure.storage;

public interface FileStorage {

    void store(byte[] content, String relativePath);

    byte[] load(String relativePath);

    void delete(String relativePath);

    boolean exists(String relativePath);
}
