package com.opus.opus.modules.file.application.convenience;

import com.opus.opus.modules.file.domain.dao.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FileTransactionHandler {

    private final FileRepository fileRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markWebpConverted(final Long fileId) {
        fileRepository.findById(fileId)
                .ifPresent(file -> file.updateIsWebpConverted(true));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteFileRecord(final Long fileId) {
        fileRepository.deleteById(fileId);
    }
}
