package com.opus.opus.global.util;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;

public class ContentDispositionUtil {

    public static String attachment(final String fileName) {
        return ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
