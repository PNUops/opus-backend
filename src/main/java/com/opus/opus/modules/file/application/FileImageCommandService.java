package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.application.event.ImageProcessingEvent;
import com.opus.opus.modules.file.application.event.PhysicalFileDeleteEvent;
import com.opus.opus.modules.file.application.processor.ImageProcessor;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImage;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import com.opus.opus.modules.file.domain.dao.FileImageRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class FileImageCommandService {

    private final FileImageRepository fileImageRepository;
    private final FilePathGenerator filePathGenerator;
    private final ImageProcessor imageProcessor;
    private final ApplicationEventPublisher eventPublisher;

    public FileImage storeImageFile(final MultipartFile multipartFile, final Long referenceId,
                                    final ReferenceDomainType referenceType, final FileImageType imageType) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileException(FileExceptionType.EMPTY_FILE);
        }

        try {
            final byte[] imageBytes = multipartFile.getBytes();
            final String relativePath = filePathGenerator.generate(imageProcessor.getOutputExtension());
            final String mimeType = multipartFile.getContentType() != null
                    ? multipartFile.getContentType() : "application/octet-stream";

            final File file = File.create(multipartFile.getOriginalFilename(), relativePath, mimeType);

            final FileImage fileImage = FileImage.builder()
                    .file(file)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .imageType(imageType)
                    .build();

            final FileImage savedFileImage = fileImageRepository.save(fileImage);

            eventPublisher.publishEvent(new ImageProcessingEvent(imageBytes, relativePath, savedFileImage.getId()));

            return savedFileImage;
        } catch (IOException e) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    public FileImage replaceImageFile(final MultipartFile multipartFile, final Long referenceId,
                                      final ReferenceDomainType referenceType, final FileImageType imageType) {
        final Optional<FileImage> existingFileImage = fileImageRepository
                .findByReferenceIdAndReferenceTypeAndImageType(referenceId, referenceType, imageType);
        final FileImage savedFileImage = storeImageFile(multipartFile, referenceId, referenceType, imageType);
        existingFileImage.ifPresent(fi -> deleteImageFile(fi.getId()));
        return savedFileImage;
    }

    public void deleteImageFile(final Long fileImageId) {
        final FileImage fileImage = fileImageRepository.findById(fileImageId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND,
                        "삭제할 파일을 찾을 수 없습니다. ID=" + fileImageId));

        final String filePath = fileImage.getFilePath();
        fileImageRepository.delete(fileImage);
        eventPublisher.publishEvent(new PhysicalFileDeleteEvent(filePath));
    }

    public void deleteIfExists(final Long referenceId, final ReferenceDomainType referenceType,
                               final FileImageType imageType) {
        fileImageRepository
                .findByReferenceIdAndReferenceTypeAndImageType(referenceId, referenceType, imageType)
                .ifPresent(fi -> deleteImageFile(fi.getId()));
    }

    public void deleteAllByReference(final Long referenceId, final ReferenceDomainType referenceType) {
        fileImageRepository
                .findAllByReferenceIdAndReferenceType(referenceId, referenceType)
                .forEach(fi -> deleteImageFile(fi.getId()));
    }
}
