package com.opus.opus.modules.file.infrastructure.processor;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebpImageProcessor implements ImageProcessor {

    private static final int WEBP_QUALITY = 80;

    @Override
    public byte[] process(final byte[] imageBytes) {
        try {
            final ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
            final WebpWriter writer = WebpWriter.DEFAULT.withQ(WEBP_QUALITY);
            return image.bytes(writer);
        } catch (Exception e) {
            log.error("WebP 이미지 변환 실패", e);
            return imageBytes;
        }
    }

    @Override
    public String getOutputExtension() {
        return "webp";
    }
}
