package com.opus.opus.modules.file.application.processor;

import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import org.springframework.stereotype.Component;

@Component
public class WebpImageProcessor implements ImageProcessor {

    private static final int WEBP_QUALITY = 80;

    @Override
    public byte[] process(final byte[] imageBytes) {
        try {
            final ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
            final WebpWriter writer = WebpWriter.DEFAULT.withQ(WEBP_QUALITY);
            return image.bytes(writer);
        } catch (Exception e) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "WebP 이미지 변환 실패", e);
        }
    }

    @Override
    public String getOutputExtension() {
        return "webp";
    }
}
