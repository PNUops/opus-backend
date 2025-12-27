package com.opus.opus.modules.file.application.convenience;

import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_THUMBNAIL;

import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileConvenience {

    private final FileRepository fileRepository;

    public File findByReferenceIdAndReferenceTypeAndImageType(final Long teamId,
                                                              final ReferenceDomainType referenceType,
                                                              final FileImageType imageType) {
        return fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, THUMBNAIL)
                .orElseThrow(() -> new FileException(NOT_EXISTS_THUMBNAIL));
    }
}
