package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.dto.FileDownload;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.FileFeedback;
import com.opus.opus.modules.file.domain.dao.FileFeedbackRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileFeedbackQueryService {

    private final FileFeedbackRepository fileFeedbackRepository;
    private final FileStorage fileStorage;

    public FileDownload download(final Long feedbackId, final Long fileId) {
        final FileFeedback fileFeedback = fileFeedbackRepository.findByIdAndFeedbackId(fileId, feedbackId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND));

        final byte[] fileBytes = fileStorage.load(fileFeedback.getFilePath());
        return new FileDownload(new ByteArrayResource(fileBytes), fileFeedback.getFileName(), fileFeedback.getMimeType());
    }
}
