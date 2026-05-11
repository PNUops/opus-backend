package com.opus.opus.modules.file.application;

import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class FilePathGenerator {

    public String generate(final String extension) {
        final String datePath = LocalDate.now().toString();
        final String filename = UUID.randomUUID() + "." + extension;
        return "files/" + datePath + "/" + filename;
    }
}
