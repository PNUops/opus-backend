package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.POSTER;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TRACK;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_EXISTS_PREVIEW;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.file.application.convenience.FileConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import com.opus.opus.modules.team.domain.Team;
import java.util.Optional;
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
    private final FileConvenience fileConvenience;
    private final FileStorageUtil fileStorageUtil;

    public ImageResponse getPreviewImage(final Long teamId, final Long imageId) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileRepository.findById(imageId).orElseThrow(() -> new FileException(NOT_EXISTS_PREVIEW));
        checkImageConverted(findFile);
        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    public ImageResponse getThumbnailImage(final Long teamId) {
        final Team team = teamConvenience.getValidateExistTeam(teamId);

        // 1. 팀 썸네일 조회
        Optional<File> teamThumbnail = fileRepository.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, THUMBNAIL);
        if (teamThumbnail.isPresent()) {
            return getImageResponse(teamThumbnail.get());
        }

        // 2. 분과(Track) 기본 썸네일 조회
        if (team.getTrackId() != null) {
            Optional<File> trackThumbnail = fileRepository.findByReferenceIdAndReferenceTypeAndImageType(team.getTrackId(), TRACK, THUMBNAIL);
            if (trackThumbnail.isPresent()) {
                return getImageResponse(trackThumbnail.get());
            }
        }

        // 3. 기본 이미지 반환
        final Pair<Resource, String> defaultResult = fileStorageUtil.findDefaultThumbnail();
        return new ImageResponse(defaultResult.a, defaultResult.b);
    }

    public ImageResponse getPosterImage(final Long teamId) {
        return getImage(teamId, POSTER);
    }

    private ImageResponse getImage(final Long teamId, final FileImageType fileImageType) {
        teamConvenience.validateExistTeam(teamId);
        final File findFile = fileConvenience.findByReferenceIdAndReferenceTypeAndImageType(teamId, TEAM, fileImageType);
        return getImageResponse(findFile);
    }

    private ImageResponse getImageResponse(final File findFile) {
        checkImageConverted(findFile);
        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findFile.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    private void checkImageConverted(final File findFile) {
        if (!findFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
