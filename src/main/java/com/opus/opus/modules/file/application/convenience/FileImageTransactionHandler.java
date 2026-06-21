package com.opus.opus.modules.file.application.convenience;

import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FileImageTransactionHandler {

    private final FileImageRepository fileImageRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWebpConverted(final Long fileImageId) {
        fileImageRepository.findById(fileImageId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND))
                .markWebpConverted();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFileImageRecord(final Long fileImageId) {
        fileImageRepository.deleteById(fileImageId);
    }
}
