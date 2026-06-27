package com.opus.opus.global.util;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;

public class ContentDispositionUtil {

    // 파일명을 UTF-8로 인코딩한 attachment용 Content-Disposition 헤더 값을 만듬 (한글 파일명 깨짐 방지)
    public static String attachment(final String fileName) {
        return ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
