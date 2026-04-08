package com.opus.opus.modules.file.infrastructure.processor;

public interface ImageProcessor {

    byte[] process(byte[] imageBytes);

    String getOutputExtension();
}
