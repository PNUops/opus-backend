package com.opus.opus.modules.contest.application;

import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.FileImageType;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestCommandService {

    private final FileStorageUtil fileStorageUtil;

    private final ContestConvenience contestConvenience;
    private final FileRepository fileRepository;

    public void saveBannerImage(Long contestId, MultipartFile image) {
        contestConvenience.getValidateExistContest(contestId);

        fileRepository.findByContestIdAndType(contestId, BANNER).ifPresent(file -> {
            checkWebpConverted(file);
            fileStorageUtil.deleteFile(file.getId());
        });
        fileStorageUtil.storeFile(image, contestId,BANNER);
    }

    private void checkWebpConverted(File existingFile) {
        if (!existingFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
