package com.opus.opus.modules.contest.application;


import static com.opus.opus.modules.contest.domain.SortType.CUSTOM;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ALREADY_CURRENT_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ALREADY_NOT_CURRENT_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.CURRENT_CONTEST_LIMIT_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.DUPLICATE_TEAM_ID_IN_SORT_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_CONTEST_SORT_CUSTOM_REQUEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_ITEM_ORDER;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.NOT_EXIST_TEAM_IN_CONTEST;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.ONLY_CUSTOM_MODE_CAN_CHANGE;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.VOTE_END_PRECEDE_VOTE_START;
import static com.opus.opus.modules.file.domain.FileImageType.BANNER;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.CONTEST;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;
import static com.opus.opus.modules.team.exception.TeamExceptionType.FAILED_TO_VALIDATE_BULK_TEAMS;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.opus.opus.global.util.ExcelTeamParser;
import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestCategoryConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestSortConvenience;
import com.opus.opus.modules.contest.application.convenience.ContestTemplateConvenience;
import com.opus.opus.modules.contest.application.dto.request.ContestRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortCustomRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestSortRequest;
import com.opus.opus.modules.contest.application.dto.request.ContestTemplateRequest;
import com.opus.opus.modules.contest.application.dto.request.TeamBulkRowDto;
import com.opus.opus.modules.contest.application.dto.request.VoteUpdateRequest;
import com.opus.opus.modules.contest.application.dto.response.ContestCurrentToggleResponse;
import com.opus.opus.modules.contest.application.dto.response.ContestResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkErrorResponse.TeamBulkError;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkUploadResponse;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkUploadResponse.TeamBulkResult;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.contest.domain.ContestCategory;
import com.opus.opus.modules.contest.domain.ContestSort;
import com.opus.opus.modules.contest.domain.ContestTemplate;
import com.opus.opus.modules.contest.domain.dao.ContestRepository;
import com.opus.opus.modules.contest.domain.dao.ContestSortRepository;
import com.opus.opus.modules.contest.domain.dao.ContestTemplateRepository;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.member.domain.dao.MemberRepository;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import com.opus.opus.modules.team.domain.dao.TeamMemberRepository;
import com.opus.opus.modules.team.domain.dao.TeamRepository;
import com.opus.opus.modules.team.exception.TeamException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final ContestSortRepository contestSortRepository;
    private final FileRepository fileRepository;
    private final ContestTemplateRepository contestTemplateRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;

    private final ContestConvenience contestConvenience;
    private final ContestCategoryConvenience contestCategoryConvenience;
    private final ContestSortConvenience contestSortConvenience;
    private final TeamConvenience teamConvenience;
    private final ContestTemplateConvenience contestTemplateConvenience;
    private final MemberConvenience memberConvenience;

    private final FileStorageUtil fileStorageUtil;
    private final ExcelTeamParser excelTeamParser;

    public void saveBannerImage(final Long contestId, final MultipartFile image) {
        contestConvenience.getValidateExistContest(contestId);

        final Optional<File> existingFile = fileRepository.findByReferenceIdAndReferenceTypeAndImageType(contestId,
                CONTEST, BANNER);
        existingFile.ifPresent(this::checkWebpConverted);

        fileStorageUtil.storeFile(image, contestId, CONTEST, BANNER);

        existingFile.ifPresent(file -> fileStorageUtil.deleteFile(file.getId()));
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

        contestSortRepository.save(ContestSort.builder()
                .contest(contest)
                .build());

        // 템플릿 자동 생성
        createTemplate(contest, contestCategory.getCategoryName());

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

    public void updateVotePeriod(final Long contestId, final VoteUpdateRequest voteRequest) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);
        checkVoteRange(voteRequest);
        contest.updateVotePeriod(voteRequest.voteStartAt(), voteRequest.voteEndAt());
    }

    private void checkVoteRange(final VoteUpdateRequest voteRequest) {
        final int compare = voteRequest.voteStartAt().compareTo(voteRequest.voteEndAt());
        if (compare > 0) {
            throw new ContestException(VOTE_END_PRECEDE_VOTE_START);
        }
    }

    public void updateMaxVotesLimit(final Long contestId, final Integer maxVotesLimit) {
        final Contest contest = contestConvenience.getValidateExistContest(contestId);

        contestConvenience.validateNotInVotingPeriod(contest);

        contest.updateMaxVotesLimit(maxVotesLimit);
    }

    public void updateContestSort(final Long contestId, final ContestSortRequest request) {
        contestConvenience.validateExistContest(contestId);
        final ContestSort contestSort = contestSortConvenience.getValidateExistContestSort(contestId);

        contestSort.updateMode(request.mode());
    }

    //todo: 팀 생성 시 itemOrder 추가
    public void updateContestSortCustom(final Long contestId, final List<ContestSortCustomRequest> requests) {
        final Contest contest = contestConvenience.getValidateExistContestForUpdate(contestId);
        final ContestSort contestSort = contestSortConvenience.getValidateExistContestSort(contest.getId());
        checkCustomSort(contestSort);
        validateDuplicateTeamIds(requests);
        validateDuplicateItemOrders(requests);

        final List<Team> teams = teamConvenience.getTeamsOfContest(contestId);
        validateRequestSizeMatchesTeams(requests, teams);
        validateItemOrderRange(requests, teams.size());

        applyCustomSortToTeams(requests, teams);
    }

    private void validateNotInVotingPeriod(final Contest contest) {
        if (contest.isVotingPeriod()) {
            throw new ContestException(CANNOT_CHANGE_VOTES_DURING_VOTING_PERIOD);
        }
    }

    private void checkWebpConverted(File existingFile) {
        if (!existingFile.getIsWebpConverted()) {
            throw new FileException(NOT_WEBP_CONVERTED);
        }
    }

    private void validateSameCurrentRequest(final Boolean currentValue, final Boolean requestValue) {
        if (currentValue.equals(requestValue)) {
            throw new ContestException(currentValue ? ALREADY_CURRENT_CONTEST : ALREADY_NOT_CURRENT_CONTEST);
        }
    }

    private void validateCurrentContestLimit(final long currentCount) {
        if (currentCount >= MAX_CURRENT_CONTEST_COUNT) {
            throw new ContestException(CURRENT_CONTEST_LIMIT_EXCEEDED);
        }
    }

    private void validateItemOrderRange(final List<ContestSortCustomRequest> requests, final int teamCount) {
        for (ContestSortCustomRequest r : requests) {
            final int order = r.itemOrder();
            if (order < 1 || order > teamCount) {
                throw new ContestException(INVALID_ITEM_ORDER);
            }
        }
    }

    private void checkCustomSort(final ContestSort contestSort) {
        if (contestSort.getMode() != CUSTOM) {
            throw new ContestException(ONLY_CUSTOM_MODE_CAN_CHANGE);
        }
    }

    private void validateRequestSizeMatchesTeams(final List<ContestSortCustomRequest> requests,
                                                 final List<Team> teams) {
        if (requests.size() != teams.size()) {
            throw new ContestException(INVALID_CONTEST_SORT_CUSTOM_REQUEST);
        }
    }

    private void validateDuplicateTeamIds(final List<ContestSortCustomRequest> requests) {
        if (requests.stream().map(ContestSortCustomRequest::teamId).distinct().count() != requests.size()) {
            throw new ContestException(DUPLICATE_TEAM_ID_IN_SORT_REQUEST);
        }
    }

    private void validateDuplicateItemOrders(final List<ContestSortCustomRequest> requests) {
        if (requests.stream().map(ContestSortCustomRequest::itemOrder).distinct().count() != requests.size()) {
            throw new ContestException(DUPLICATE_ITEM_ORDER_IN_SORT_REQUEST);
        }
    }

    private void applyCustomSortToTeams(final List<ContestSortCustomRequest> requests, final List<Team> teams) {
        final Map<Long, Team> teamMap = teams.stream()
                .collect(toMap(Team::getId, identity()));

        for (ContestSortCustomRequest r : requests) {
            final Team team = teamMap.get(r.teamId());
            if (team == null) {
                throw new ContestException(NOT_EXIST_TEAM_IN_CONTEST);
            }
            team.updateItemOrder(r.itemOrder());
        }
    }

    public void createTemplate(final Contest contest, final String categoryName) {
        final ContestTemplateRequest request = getDefaultTemplateRequest(categoryName);

        final ContestTemplate template = ContestTemplate.builder()
                .contest(contest)
                .request(request)
                .build();

        contestTemplateRepository.save(template);
    }

    public void updateContestTemplate(final Long contestId, final ContestTemplateRequest request) {
        contestConvenience.validateExistContest(contestId);
        final ContestTemplate template = contestTemplateConvenience.getValidateExistTemplate(contestId);
        template.updateTemplate(request);
    }

    private ContestTemplateRequest getDefaultTemplateRequest(final String categoryName) {
        if (categoryName != null && categoryName.contains("창의융합")) {
            return new ContestTemplateRequest(
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true,
                    false,
                    false,
                    true,
                    true,
                    true
            );
        }

        if (categoryName != null && categoryName.contains("캡스톤")) {
            return new ContestTemplateRequest(
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    false,
                    true,
                    false,
                    true
            );
        }

        return new ContestTemplateRequest(
                false, false, false, false, false, false, false, false, false, false, false, false
        );
    }

    public TeamBulkUploadResponse bulkUploadTeams(final Long contestId, final MultipartFile file) {
        contestConvenience.validateExistContest(contestId);

        final List<TeamBulkRowDto> rows = excelTeamParser.parseAndValidate(file);

        final List<TeamBulkError> errorList = validateRows(rows, contestId);
        if (!errorList.isEmpty()) {
            throw new TeamException(FAILED_TO_VALIDATE_BULK_TEAMS, errorList);
        }

        return saveTeams(rows, contestId);
    }

    private List<TeamBulkError> validateRows(final List<TeamBulkRowDto> rows, final Long contestId) {
        final List<TeamBulkError> errors = new ArrayList<>();

        final Set<String> seenStudentIds = new HashSet<>();
        final Set<String> seenEmails = new HashSet<>();
        final Set<String> seenTeamNames = new HashSet<>();

        for (final TeamBulkRowDto row : rows) {
            final int rowNum = row.rowNumber();

            if (isBlank(row.teamName())) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀 이름은 필수입니다."));
            } else if (!seenTeamNames.add(row.teamName())) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀 이름 '" + row.teamName() + "'이 파일 내에서 중복됩니다."));
            }

            if (isBlank(row.projectName())) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 프로젝트 이름은 필수입니다."));
            }
            if (isBlank(row.leaderName())) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀장 이름은 필수입니다."));
            }
            if (isBlank(row.leaderStudentId())) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀장 학번은 필수입니다."));
            }
            if (isBlank(row.leaderEmail())) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀장 이메일은 필수입니다."));
            }

            // 팀원 이름/학번/이메일 개수 일치 검사
            if (row.memberNames().size() != row.memberStudentIds().size()
                    || row.memberNames().size() != row.memberEmails().size()) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀원 이름, 학번, 이메일의 개수가 일치하지 않습니다."));
            }

            // 학번 형식 및 중복 검사 (팀장 + 팀원)
            validateStudentIdAndEmail(row.leaderStudentId(), row.leaderEmail(), rowNum, seenStudentIds, seenEmails, errors);
            for (int i = 0; i < row.memberStudentIds().size(); i++) {
                validateStudentIdAndEmail(
                        row.memberStudentIds().get(i),
                        i < row.memberEmails().size() ? row.memberEmails().get(i) : null,
                        rowNum, seenStudentIds, seenEmails, errors);
            }
        }

        if (errors.isEmpty()) {
            validateAgainstDatabase(rows, contestId, errors);
        }

        return errors;
    }

    private void validateStudentIdAndEmail(final String studentId, final String email,
                                            final int rowNum, final Set<String> seenStudentIds,
                                            final Set<String> seenEmails, final List<TeamBulkError> errors) {
        if (studentId != null && !studentId.isBlank()) {
            if (!studentId.matches("\\d{7,9}")) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 학번 형식이 올바르지 않습니다."));
            }
            if (!seenStudentIds.add(studentId)) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 학번 " + studentId + "가 파일 내에서 중복됩니다."));
            }
        }

        if (email != null && !email.isBlank()) {
            if (!email.endsWith("@pusan.ac.kr")) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 부산대 이메일(@pusan.ac.kr)만 허용됩니다."));
            }
            if (!seenEmails.add(email)) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 이메일 " + email + "이 파일 내에서 중복됩니다."));
            }
        }
    }

    private void validateAgainstDatabase(final List<TeamBulkRowDto> rows, final Long contestId,
                                          final List<TeamBulkError> errors) {
        final List<Team> existingTeams = teamRepository.findAllByContestId(contestId);

        final Set<String> existingTeamNames = existingTeams.stream()
                .map(Team::getTeamName)
                .collect(toSet());

        final Set<Long> existingMemberIds = existingTeams.stream()
                .flatMap(team -> team.getTeamMembers().stream())
                .map(TeamMember::getMemberId)
                .collect(toSet());

        for (final TeamBulkRowDto row : rows) {
            if (existingTeamNames.contains(row.teamName())) {
                errors.add(new TeamBulkError(row.rowNumber(),
                        row.rowNumber() + "번째 행: 팀 이름 '" + row.teamName() + "'이 해당 대회에 이미 존재합니다."));
            }

            // 팀장 + 팀원의 이메일/학번으로 기존 회원 조회 → 대회 내 중복 소속 검사 + studentId 충돌 검사
            checkMemberAgainstDatabase(row.leaderEmail(), row.rowNumber(), row.leaderStudentId(),
                    existingMemberIds, errors);
            for (int i = 0; i < row.memberEmails().size(); i++) {
                checkMemberAgainstDatabase(row.memberEmails().get(i), row.rowNumber(),
                        row.memberStudentIds().get(i), existingMemberIds, errors);
            }
        }
    }

    private void checkMemberAgainstDatabase(final String email, final int rowNum, final String studentId,
                                              final Set<Long> existingMemberIds, final List<TeamBulkError> errors) {
        // 이메일로 기존 회원 조회 → 대회 내 중복 소속 검사
        memberRepository.findByEmail(email).ifPresent(member -> {
            if (existingMemberIds.contains(member.getId())) {
                errors.add(new TeamBulkError(rowNum,
                        rowNum + "번째 행: " + studentId + " 학생이 해당 대회의 다른 팀에 이미 소속되어 있습니다."));
            }
        });

        // 학번으로 기존 회원 조회 → 이메일이 다른 회원이 같은 학번을 사용하는지 검사
        if (studentId != null && !studentId.isBlank()) {
            memberRepository.findByStudentId(studentId).ifPresent(member -> {
                if (!email.equals(member.getEmail())) {
                    errors.add(new TeamBulkError(rowNum,
                            rowNum + "번째 행: 학번 " + studentId + "이 다른 이메일로 이미 등록되어 있습니다."));
                }
            });
        }
    }

    private TeamBulkUploadResponse saveTeams(final List<TeamBulkRowDto> rows, final Long contestId) {
        final int existingTeamCount = teamRepository.findAllByContestId(contestId).size();
        final List<TeamBulkResult> results = new ArrayList<>();

        for (int i = 0; i < rows.size(); i++) {
            final TeamBulkRowDto row = rows.get(i);

            // 팀 생성
            final Team team = Team.builder()
                    .teamName(row.teamName())
                    .projectName(row.projectName())
                    .contestId(contestId)
                    .itemOrder(existingTeamCount + i + 1)
                    .build();
            teamRepository.save(team);

            // 팀장 매핑/생성 + 팀원 등록
            final Member leader = getOrCreateMember(row.leaderEmail(), row.leaderStudentId(), row.leaderName());
            saveTeamMember(leader, team, TeamMemberRoleType.ROLE_팀장);

            // 팀원 매핑/생성 + 팀원 등록
            for (int j = 0; j < row.memberNames().size(); j++) {
                final Member member = getOrCreateMember(
                        row.memberEmails().get(j),
                        row.memberStudentIds().get(j),
                        row.memberNames().get(j));
                saveTeamMember(member, team, TeamMemberRoleType.ROLE_팀원);
            }

            results.add(new TeamBulkResult(row.rowNumber(), row.teamName(), team.getId()));
        }

        return new TeamBulkUploadResponse(results.size(), results);
    }

    private Member getOrCreateMember(final String email, final String studentId, final String name) {
        return memberRepository.findByEmail(email)
                .map(member -> {
                    if (member.getStudentId() == null && studentId != null) {
                        member.updateStudentId(studentId);
                    }
                    return member;
                })
                .orElseGet(() -> memberConvenience.getOrCreateFakeMemberByEmail(email, studentId, name));
    }

    private void saveTeamMember(final Member member, final Team team, final TeamMemberRoleType roleType) {
        final TeamMember teamMember = TeamMember.builder()
                .memberId(member.getId())
                .team(team)
                .roles(Set.of(roleType))
                .build();
        teamMemberRepository.save(teamMember);
    }

    private boolean isBlank(final String value) {
        return value == null || value.isBlank();
    }
}
