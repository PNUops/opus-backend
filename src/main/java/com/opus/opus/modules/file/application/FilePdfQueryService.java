package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.processor.PdfProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilePdfQueryService {

    private final PdfProcessor pdfProcessor;

    // 다른 모듈은 file 모듈 서비스를 통해서만 PDF를 만든다. (ImageProcessor를 FileImageCommandService가 감싸는 것과 같은 방식)
    public byte[] generatePdf(final String html) {
        return pdfProcessor.process(html);
    }

    // PDF 템플릿 CSS의 font-family에 쓸 폰트 이름 (PdfProcessor가 등록한 폰트)
    public String getFontFamily() {
        return PdfProcessor.FONT_FAMILY;
    }
}
