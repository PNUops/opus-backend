package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.file.exception.FileExceptionType;
import com.opus.opus.modules.team.application.dto.ImageResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContestQueryService {

    private final FileStorageUtil fileStorageUtil;

    private final ContestRepository contestRepository;
    private final FileRepository fileRepository;

    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestConvenience contestConvenience;

    public ImageResponse findContestBanner(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);
        final File findBanner = fileRepository.findByReferenceIdAndReferenceTypeAndImageType(contestId, CONTEST, BANNER)
                .orElseThrow(() -> new FileException(FileExceptionType.NOT_EXISTS_BANNER));

        checkImageConverted(findBanner);

        final Pair<Resource, String> storageResult = fileStorageUtil.findFileAndType(findBanner.getId());
        return new ImageResponse(storageResult.a, storageResult.b);
    }

    public List<ContestCurrentResponse> getCurrentContests() {
        List<Contest> contests = contestConvenience.getCurrentContests();

        return contests.stream()
                .map(contest -> {
                    String categoryName = contestCategoryConvenience.getValidateExistCategory(contest.getCategoryId())
                            .getCategoryName();
                    return ContestCurrentResponse.of(contest, categoryName);
                })
                .toList();
    }

    public List<ContestResponse> getAllContests() {
        List<Contest> contests = contestRepository.findAll();

        return contests.stream()
                .map(contest -> {
                    ContestCategory category = contestCategoryConvenience.getValidateExistCategory(
                            contest.getCategoryId());
                    return ContestResponse.from(contest, category.getCategoryName());
                })
                .toList();

    }

    private void checkImageConverted(final File findFile) {
        if (!findFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }
}
