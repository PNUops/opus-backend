package com.opus.opus.file.application;

import static com.opus.opus.modules.file.exception.FileExceptionType.PDF_GENERATION_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.opus.opus.helper.FileModuleIntegrationTest;
import com.opus.opus.modules.file.application.FilePdfQueryService;
import com.opus.opus.modules.file.exception.FileException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FilePdfQueryServiceTest extends FileModuleIntegrationTest {

    @Autowired
    private FilePdfQueryService filePdfQueryService;

    @Test
    @DisplayName("[성공] 한글이 포함된 HTML을 PDF로 변환한다.")
    void 한글이_포함된_HTML을_PDF로_변환한다() throws Exception {
        final String html = """
                <html>
                <head><style>body { font-family: '%s'; }</style></head>
                <body><p>온라인 투표 결과</p></body>
                </html>
                """.formatted(filePdfQueryService.getFontFamily());

        final byte[] pdf = filePdfQueryService.generatePdf(html);

        try (PDDocument document = Loader.loadPDF(pdf)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
            assertThat(new PDFTextStripper().getText(document)).contains("온라인 투표 결과");
        }
    }

    @Test
    @DisplayName("[실패] XHTML 형식이 아닌 HTML은 PDF로 변환할 수 없다.")
    void XHTML_형식이_아닌_HTML은_PDF로_변환할_수_없다() {
        final String html = "<html><body><p>닫히지 않은 태그</body></html>";

        assertThatThrownBy(() -> filePdfQueryService.generatePdf(html))
                .isInstanceOf(FileException.class)
                .satisfies(e -> assertThat(((FileException) e).exceptionType()).isEqualTo(PDF_GENERATION_FAILED));
    }
}
