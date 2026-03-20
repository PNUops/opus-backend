package com.opus.opus.modules.file.application.convenience;

import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;

import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import java.util.List;
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
        return fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, referenceType, imageType)
                .orElseThrow(() -> new FileException(NOT_EXISTS_MATCHING_IMAGE_ID));
    }

    public List<Long> findAllPreviewIdsByTeamId(final Long teamId) {
        return fileRepository.findAllByReferenceIdAndReferenceTypeAndImageType(teamId, ReferenceDomainType.TEAM,
                        FileImageType.PREVIEW)
                .stream()
                .map(File::getId)
                .sorted()
                .toList();
    }
}
