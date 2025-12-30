package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.contest.exception.ContestExceptionType.ALREADY_CURRENT_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ALREADY_NOT_CURRENT_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CURRENT_CONTEST_LIMIT_EXCEEDED;
import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentToggleResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ContestCommandService {

    private static final int MAX_CURRENT_CONTEST_COUNT = 2;

    private final ContestRepository contestRepository;
    private final FileRepository fileRepository;

    private final ContestConvenience contestConvenience;
    private final ContestCategoryConvenience contestCategoryConvenience;
    private final TeamConvenience teamConvenience;

    private final FileStorageUtil fileStorageUtil;


    public void saveBannerImage(final Long contestId, final MultipartFile image) {
        contestConvenience.getValidateExistContest(contestId);

        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(contestId, CONTEST, BANNER).ifPresent(file -> {
            checkWebpConverted(file);
            fileStorageUtil.deleteFile(file.getId());
        });
        fileStorageUtil.storeFile(image, contestId, CONTEST, BANNER);
    }

    public void deleteBannerImage(final Long contestId) {
        contestConvenience.getValidateExistContest(contestId);

        fileRepository.findByReferenceIdAndReferenceTypeAndImageType(contestId, CONTEST, BANNER).ifPresent(file ->
                fileStorageUtil.deleteFile(file.getId())
        );
    }

    public ContestResponse createContest(final ContestRequest request) {
        contestConvenience.validateDuplicateContestName(request.contestName());
        ContestCategory contestCategory = contestCategoryConvenience.getValidateExistCategory(request.categoryId());
        final Contest contest = Contest.builder()
                .contestName(request.contestName())
                .categoryId(request.categoryId())
                .build();
        contestRepository.save(contest);

        return ContestResponse.from(contest, contestCategory.getCategoryName());
    }

    public void updateContest(final Long contestId, final ContestRequest request) {
        contestConvenience.validateDuplicateContestName(request.contestName());
        contestCategoryConvenience.getValidateExistCategory(request.categoryId());
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        contest.updateContest(request.categoryId(), request.contestName());
    }

    public void deleteContest(final Long contestId) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        teamConvenience.validateAllTeamsDeletedInContest(contestId);
        contestRepository.delete(contest);
    }

    public ContestCurrentToggleResponse toggleCurrent(final Long contestId, final Boolean isCurrent) {
        Contest contest = contestConvenience.getValidateExistContest(contestId);
        // 기존과 동일한 값 요청이면 예외
        validateSameCurrentRequest(contest.getIsCurrent(), isCurrent);
        // 현재 대회로 등록하는 경우, 현재 진행 중인 대회 최대 개수 검사
        if (isCurrent) {
            long currentCount = contestConvenience.countCurrentContests();
            validateCurrentContestLimit(currentCount);
        }
        // 변경
        contest.updateIsCurrent(isCurrent);
        return ContestCurrentToggleResponse.of(contest.getId(), isCurrent);
    }

    private void checkWebpConverted(File existingFile) {
        if (!existingFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }

    private void validateSameCurrentRequest(final Boolean currentValue, final Boolean requestValue) {
        if (currentValue == requestValue) {
            throw new ContestException(currentValue ? ALREADY_CURRENT_CONTEST : ALREADY_NOT_CURRENT_CONTEST);
        }
    }

    private void validateCurrentContestLimit(final long currentCount) {
        if (currentCount >= MAX_CURRENT_CONTEST_COUNT) {
            throw new ContestException(CURRENT_CONTEST_LIMIT_EXCEEDED);
        }
    }
}
