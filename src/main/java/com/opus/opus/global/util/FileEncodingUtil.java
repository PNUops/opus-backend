package com.opus.opus.global.util;

import com.ops.ops.modules.file.domain.dao.FileRepository;
import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileEncodingUtil {

    private final FileRepository fileRepository;

    @Async("imageTaskExecutor")
    @Transactional
    public void convertToWebpAndSave(byte[] imageBytes, Path webpFilePath, Long fileId) {
        try {
            ImmutableImage image = ImmutableImage.loader().fromBytes(imageBytes);
            WebpWriter writer = WebpWriter.DEFAULT.withQ(80);

            image.output(writer, webpFilePath);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        fileRepository.findById(fileId).ifPresent(file -> file.updateIsWebpConverted(true));
    }

}
