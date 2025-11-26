package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_PREVIEW;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_THUMBNAIL;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamQueryService {

    private final TeamConvenience teamConvenience;
    private final FileRepository fileRepository;
    private final FileStorageUtil fileStorageUtil;

    public ImageResponse findPreviewImage(Long teamId, Long imageId) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileRepository.findById(imageId).orElseThrow(() -> new FileException(NOT_EXISTS_PREVIEW));
        checkImageConverted(findFile);
        Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    public ImageResponse findThumbnailImage(final Long teamId) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileRepository.findByTeamIdAndType(teamId, THUMBNAIL)
                .orElseThrow(() -> new FileException(NOT_EXISTS_THUMBNAIL));
        checkImageConverted(findFile);
        Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    private void checkImageConverted(File findFile) {
        if (!findFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
