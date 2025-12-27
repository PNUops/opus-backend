package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.PREVIEW;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.EXCEED_PREVIEW_LIMIT;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamCommandService {

    private final FileStorageUtil fileStorageUtil;

    private final FileRepository fileRepository;
    private final TeamConvenience teamConvenience;


    public void savePreviewImages(final Long teamId, final List<MultipartFile> images) {
        teamConvenience.validateExistTeam(teamId);
        checkPreviewLimit(teamId, images);
        for (MultipartFile image : images) {
            fileStorageUtil.storeFile(image, teamId, TEAM, PREVIEW);
        }
    }

    public void deletePreviewImages(final Long teamId, final List<Long> ids) {
        teamConvenience.validateExistTeam(teamId);
        ids.forEach(fileId -> {
            fileRepository.findById(fileId).ifPresent(this::checkWebpConverted);
            fileStorageUtil.deleteFile(fileId);
        });
    }

    public void saveThumbnailImage(final Long teamId, final MultipartFile image) {
        teamConvenience.validateExistTeam(teamId);
        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, THUMBNAIL).ifPresent(existingFile -> {
            checkWebpConverted(existingFile);
            fileStorageUtil.deleteFile(existingFile.getId());
        });
        fileStorageUtil.storeFile(image, teamId, TEAM, THUMBNAIL);
    }

    public void deleteThumbnailImage(final Long teamId) {
        teamConvenience.validateExistTeam(teamId);
        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, THUMBNAIL).ifPresent(existingFile -> {
            checkWebpConverted(existingFile);
            fileStorageUtil.deleteFile(existingFile.getId());
        });
    }

    private void checkPreviewLimit(final Long teamId, final List<MultipartFile> images) {
        long savedCount = fileRepository.countByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, PREVIEW);
        if (savedCount + images.size() > 5) {
            throw new FileException(EXCEED_PREVIEW_LIMIT);
        }
    }

    private void checkWebpConverted(final File existingFile) {
        if (!existingFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
