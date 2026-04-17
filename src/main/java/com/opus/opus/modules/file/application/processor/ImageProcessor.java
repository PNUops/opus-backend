package com.opus.opus.modules.file.application.processor;

public interface ImageProcessor {

    byte[] process(byte[] imageBytes);

    String getOutputExtension();
}
