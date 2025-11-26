package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestQueryService {

    private final ContestConvenience contestConvenience;
    private final FileRepository fileRepository;
    private final FileStorageUtil fileStorageUtil;

    public ImageResponse findContestBanner(Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        File findBanner = fileRepository.findByContestIdAndType(contestId, BANNER)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_EXISTS_BANNER));

        checkImageConverted(findBanner);

        Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findBanner.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    private void checkImageConverted(File findFile) {
        if (!findFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
