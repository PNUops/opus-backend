package com.opus.opus.modules.file.application;

import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import java.util.Optional;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.opus.opus.modules.file.application.processor.ImageProcessor;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class FileCommandService {

    private final FileRepository fileRepository;
    private final FilePathGenerator filePathGenerator;
    private final ImageProcessor imageProcessor;
    private final AsyncImageProcessingService asyncImageProcessingService;

    public File storeImageFile(final MultipartFile multipartFile, final Long referenceId,
                               final ReferenceDomainType referenceType, final FileImageType imageType) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileException(FileExceptionType.EMPTY_FILE);
        }

        try {
            final byte[] imageBytes = multipartFile.getBytes();
            final String relativePath = filePathGenerator.generate(imageProcessor.getOutputExtension());

            final File savedFile = fileRepository.save(File.builder()
                    .name(multipartFile.getOriginalFilename())
                    .filePath(relativePath)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .imageType(imageType)
                    .build());

            asyncImageProcessingService.processAndStore(imageBytes, relativePath, savedFile.getId());

            return savedFile;
        } catch (IOException e) {
            throw new FileException(FileExceptionType.SAVE_FAILED, "파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    public File replaceImageFile(final MultipartFile multipartFile, final Long referenceId,
                                final ReferenceDomainType referenceType, final FileImageType imageType) {
        final Optional<File> existingFile = fileRepository.findByReferenceIdAndReferenceTypeAndImageType(
                referenceId, referenceType, imageType);
        final File savedFile = storeImageFile(multipartFile, referenceId, referenceType, imageType);
        existingFile.ifPresent(file -> deleteFile(file.getId()));
        return savedFile;
    }

    public void deleteFile(final Long fileId) {
        final File fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_FOUND,
                        "삭제할 파일을 찾을 수 없습니다. ID=" + fileId));

        asyncImageProcessingService.deletePhysicalFile(fileEntity.getFilePath());
        fileRepository.delete(fileEntity);
    }
}
