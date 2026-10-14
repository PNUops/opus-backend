package com.opus.opus.modules.file.application.processor;

import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder.FontStyle;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.io.ByteArrayOutputStream;
import org.springframework.stereotype.Component;

@Component
public class PdfProcessor {

    // 프론트와 같은 Noto Sans KR. 공식 가변 폰트에서 wght 400/700 정적 인스턴스를 만들고
    // 한글·영문·기호만 남긴 서브셋이다. (한자·일본어 제외, OFL 1.1)
    public static final String FONT_FAMILY = "Noto Sans KR";

    private static final String REGULAR_FONT_PATH = "/fonts/NotoSansKR-Regular.ttf";
    private static final String BOLD_FONT_PATH = "/fonts/NotoSansKR-Bold.ttf";
    private static final int REGULAR_FONT_WEIGHT = 400;
    private static final int BOLD_FONT_WEIGHT = 700;

    // XHTML 형식의 HTML을 한글 폰트를 포함한 PDF로 변환한다. (폰트는 사용한 글자만 서브셋으로 임베딩)
    public byte[] process(final String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            final PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFont(() -> PdfProcessor.class.getResourceAsStream(REGULAR_FONT_PATH),
                    FONT_FAMILY, REGULAR_FONT_WEIGHT, FontStyle.NORMAL, true);
            builder.useFont(() -> PdfProcessor.class.getResourceAsStream(BOLD_FONT_PATH),
                    FONT_FAMILY, BOLD_FONT_WEIGHT, FontStyle.NORMAL, true);
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new FileException(FileExceptionType.PDF_GENERATION_FAILED, "PDF 변환 실패", e);
        }
    }
}
