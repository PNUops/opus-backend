package com.opus.opus.modules.file.application.convenience;

import static com.opus.opus.modules.file.domain.FileImageType.PREVIEW;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_PREVIEW;

import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import com.opus.opus.modules.file.exception.FileException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileImageConvenience {

    private final FileImageRepository fileImageRepository;

    public FileImage findByReferenceIdAndReferenceTypeAndImageType(final Long referenceId,
                                                                   final ReferenceDomainType referenceType,
                                                                   final FileImageType imageType) {
        return fileImageRepository
                .findByReferenceIdAndReferenceTypeAndImageType(referenceId, referenceType, imageType)
                .orElseThrow(() -> new FileException(NOT_EXISTS_MATCHING_IMAGE_ID));
    }

    public List<Long> findAllPreviewFileIdsByTeamId(final Long teamId) {
        return fileImageRepository
                .findAllByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, PREVIEW)
                .stream()
                .map(FileImage::getId)
                .sorted()
                .toList();
    }

    public long countByReferenceIdAndReferenceTypeAndImageType(final Long referenceId,
                                                               final ReferenceDomainType referenceType,
                                                               final FileImageType imageType) {
        return fileImageRepository
                .countByReferenceIdAndReferenceTypeAndImageType(referenceId, referenceType, imageType);
    }

    public Optional<FileImage> findOptionalByReferenceIdAndReferenceTypeAndImageType(
            final Long referenceId, final ReferenceDomainType referenceType, final FileImageType imageType) {
        return fileImageRepository
                .findByReferenceIdAndReferenceTypeAndImageType(referenceId, referenceType, imageType);
    }

    public FileImage findByFileImageId(final Long fileImageId) {
        return fileImageRepository.findById(fileImageId)
                .orElseThrow(() -> new FileException(NOT_EXISTS_PREVIEW));
    }
}
