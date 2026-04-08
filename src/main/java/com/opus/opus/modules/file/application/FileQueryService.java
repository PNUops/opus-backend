package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.opus.opus.modules.file.infrastructure.storage.FileStorage;
import jakarta.activation.MimetypesFileTypeMap;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileQueryService {

    private static final String DEFAULT_THUMBNAIL_PATH = "default_thumbnail.jpg";
    private static final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

    static {
        mimeTypesMap.addMimeTypes("image/webp webp WEBP");
    }

    private final FileStorage fileStorage;
    private final FileRepository fileRepository;

    public Pair<Resource, String> findFileAndType(final Long fileId) {
        final File findFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID));

        final byte[] fileBytes = fileStorage.load(findFile.getFilePath());
        final ByteArrayResource resource = new ByteArrayResource(fileBytes);
        final String mimeType = mimeTypesMap.getContentType(findFile.getFilePath());

        return new Pair<>(resource, mimeType);
    }

    public Pair<Resource, String> findDefaultThumbnail() {
        final byte[] fileBytes = fileStorage.load(DEFAULT_THUMBNAIL_PATH);
        final ByteArrayResource resource = new ByteArrayResource(fileBytes);
        final String mimeType = mimeTypesMap.getContentType(DEFAULT_THUMBNAIL_PATH);

        return new Pair<>(resource, mimeType);
    }
}
