package com.opus.opus.global.util;

import java.nio.charset.StandardCharsets;
import org.springframework.http.ContentDisposition;

public class FileDownloadUtil {

    // 파일 다운로드 응답에 사용할 응답 헤더 생성
    public static String attachmentHeader(final String fileName) {
        return ContentDisposition.attachment()
                .filename(fileName, StandardCharsets.UTF_8)
                .build()
                .toString();
    }
}
