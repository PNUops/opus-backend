package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.dto.DocumentFileDownload;
import com.opus.opus.modules.file.application.storage.FileStorage;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileDocument;
import com.opus.opus.modules.file.domain.dao.FileDocumentRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileDocumentQueryService {

    private final FileDocumentRepository fileDocumentRepository;
    private final FileStorage fileStorage;

    // 단일 파일 다운로드용. 파일 바이트 전체를 메모리에 적재해 응답에 필요한 메타데이터와 함께 반환한다.
    public DocumentFileDownload download(final Long fileDocumentId) {
        final FileDocument fileDocument = fileDocumentRepository.findById(fileDocumentId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND));

        final File file = fileDocument.getFile();
        final Resource resource = new ByteArrayResource(fileStorage.load(file.getFilePath()));

        return new DocumentFileDownload(
                resource,
                file.getName(),
                file.getMimeType(),
                file.getFileSize(),
                fileDocument.getSubmissionId()
        );
    }

    // zip 파일 스트리밍용. 메모리에 적재하지 않고 파일을 스트림으로 열어 그대로 흘려보낸다.
    public InputStream openStream(final String filePath) {
        return fileStorage.loadAsStream(filePath);
    }
}
