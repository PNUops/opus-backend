package com.opus.opus.global.util;


import com.opus.opus.global.error.FileDeleteFailedException;
import com.opus.opus.global.error.FileNotFoundException;
import com.opus.opus.global.error.FileSaveFailedException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.ReferenceDomainType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import jakarta.activation.MimetypesFileTypeMap;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileStorageUtil {

    private static final Path ROOT_PATH = Paths.get(System.getProperty("user.dir"));
    private static final Path RESOURCE_PATH = ROOT_PATH.resolve("src/main/resources/opus_files");
    private static final Path DEFAULT_FILE_PATH = RESOURCE_PATH.resolve("files");
    private static final String DEFAULT_THUMBNAIL_FILENAME = "default_thumbnail.jpg";
    private static final Path DEFAULT_THUMBNAIL_PATH = RESOURCE_PATH.resolve(DEFAULT_THUMBNAIL_FILENAME);

    private static final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

    private final FileRepository fileRepository;
    private final FileEncodingUtil fileEncodingUtil;
    private final EntityManager em;

    static {
        try {
            Files.createDirectories(DEFAULT_FILE_PATH);
        } catch (IOException ignored) {
        }
        mimeTypesMap.addMimeTypes("image/webp webp WEBP");
    }

    public Pair<Resource, String> findFileAndType(final Long fileId) {
        final File findFile = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_EXISTS_MATCHING_IMAGE_ID));
        final ByteArrayResource findResource =
                findPhysicalFile(RESOURCE_PATH.resolve(findFile.getFilePath()).normalize());

        final String mimeType = mimeTypesMap
                .getContentType(RESOURCE_PATH.resolve(findFile.getFilePath()).normalize().toFile());
        return new Pair<>(findResource, mimeType);
    }

    public Pair<Resource, String> findDefaultThumbnail() {
        final ByteArrayResource findResource = findPhysicalFile(DEFAULT_THUMBNAIL_PATH);

        final String mimeType = mimeTypesMap.getContentType(DEFAULT_THUMBNAIL_PATH.toFile());

        return new Pair<>(findResource, mimeType);
    }

    private ByteArrayResource findPhysicalFile(Path filePath) {
        if (!Files.exists(filePath)) {
            throw new FileException(FileExceptionType.NOT_EXISTS_PHYSICAL_FILE);
        }

        byte[] fileBytes = null;
        try {
            fileBytes = Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileException(FileExceptionType.NOT_EXISTS_PHYSICAL_FILE);
        }
        return new ByteArrayResource(fileBytes);
    }

    public File storeFile(final MultipartFile multipartFile, final Long referenceId,
                          final ReferenceDomainType referenceType, final FileImageType type) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new FileSaveFailedException("업로드할 파일이 비어 있거나 존재하지 않습니다.");
        }

        try {
            final LocalDate today = LocalDate.now();
            final Path uploadDir = DEFAULT_FILE_PATH.resolve(today.toString());
            if (Files.notExists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            final String originalFilename = multipartFile.getOriginalFilename();
            String extension = "";
            if (originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            final String randomFilename = UUID.randomUUID() + extension;
            final Path targetFile = uploadDir.resolve(randomFilename);
            final Path webpFilePath = getWebpFilePath(targetFile);

            final Path relativePath = RESOURCE_PATH.relativize(webpFilePath);
            final String filePathForDb = relativePath.toString().replace("\\", "/");

            File savedFile = fileRepository.save(File.builder()
                    .name(originalFilename)
                    .filePath(filePathForDb)
                    .referenceId(referenceId)
                    .referenceType(referenceType)
                    .imageType(type)
                    .build());
            em.flush();

            final byte[] fileBytes = multipartFile.getBytes();
            fileEncodingUtil.convertToWebpAndSave(fileBytes, webpFilePath, savedFile.getId());

            return savedFile;
        } catch (IOException e) {
            throw new FileSaveFailedException("로컬 디스크에 파일을 저장하는 중 오류가 발생했습니다.", e);
        }
    }

    private Path getWebpFilePath(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        String newFileName = fileName.substring(0, lastDotIndex) + ".webp";
        return filePath.getParent().resolve(newFileName);
    }

    public void deleteFile(final Long fileId) {
        final File fileEntity = fileRepository.findById(fileId)
                .orElseThrow(() -> new FileNotFoundException("삭제할 파일을 찾을 수 없습니다. ID=" + fileId));

        deletePhysicalFile(fileEntity);
        fileRepository.delete(fileEntity);
    }

    private void deletePhysicalFile(final File fileEntity) {
        final Path fullPath = RESOURCE_PATH.resolve(fileEntity.getFilePath());
        if (Files.exists(fullPath)) {
            try {
                Files.delete(fullPath);
            } catch (IOException | SecurityException e) {
                throw new FileDeleteFailedException("물리 파일 삭제에 실패했습니다. 경로=" + fullPath, e);
            }
        } else {
            log.error("삭제하려는 물리 파일이 존재하지 않습니다: {}", fullPath);
        }
    }
}
