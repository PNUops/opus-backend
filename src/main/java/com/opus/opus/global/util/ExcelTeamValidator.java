package com.opus.opus.global.util;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.EMPTY_TEAM_DATA;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.FILE_REQUIRED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.FILE_SIZE_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_FILE_FORMAT;
import static java.util.stream.Collectors.toSet;

import com.opus.opus.modules.contest.application.dto.request.TeamBulkRowDto;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkErrorResponse.TeamBulkError;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ExcelTeamValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final TeamConvenience teamConvenience;
    private final MemberConvenience memberConvenience;

    public void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ContestException(FILE_REQUIRED);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ContestException(FILE_SIZE_EXCEEDED);
        }
        final String filename = file.getOriginalFilename();
        if (filename == null || !filename.endsWith(".xlsx")) {
            throw new ContestException(INVALID_FILE_FORMAT);
        }
    }

    public void validateNotEmpty(final List<TeamBulkRowDto> rows) {
        if (rows.isEmpty()) {
            throw new ContestException(EMPTY_TEAM_DATA);
        }
    }

    public List<TeamBulkError> validateRows(final List<TeamBulkRowDto> rows, final Long contestId) {
        final List<TeamBulkError> errors = new ArrayList<>();

        validateDataIntegrity(rows, errors);

        if (errors.isEmpty()) {
            validateAgainstDatabase(rows, contestId, errors);
        }

        return errors;
    }

    private void validateDataIntegrity(final List<TeamBulkRowDto> rows, final List<TeamBulkError> errors) {
        final Set<String> seenStudentIds = new HashSet<>();
        final Set<String> seenEmails = new HashSet<>();
        final Set<String> seenTeamNames = new HashSet<>();

        for (final TeamBulkRowDto row : rows) {
            final int rowNum = row.rowNumber();

            validateRequiredFields(row, rowNum, seenTeamNames, errors);
            validateMemberCount(row, rowNum, errors);
            validateStudentIdAndEmail(row.leaderStudentId(), row.leaderEmail(), rowNum, seenStudentIds, seenEmails, errors);

            for (int i = 0; i < row.memberStudentIds().size(); i++) {
                validateStudentIdAndEmail(
                        row.memberStudentIds().get(i),
                        i < row.memberEmails().size() ? row.memberEmails().get(i) : null,
                        rowNum, seenStudentIds, seenEmails, errors
                );
            }
        }
    }

    private void validateRequiredFields(final TeamBulkRowDto row, final int rowNum,
                                         final Set<String> seenTeamNames, final List<TeamBulkError> errors) {
        if (checkBlank(row.teamName())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀 이름은 필수입니다."));
        } else if (!seenTeamNames.add(row.teamName())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀 이름 '" + row.teamName() + "'이 파일 내에서 중복됩니다."));
        }

        if (checkBlank(row.projectName())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 프로젝트 이름은 필수입니다."));
        }
        if (checkBlank(row.leaderName())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀장 이름은 필수입니다."));
        }
        if (checkBlank(row.leaderStudentId())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀장 학번은 필수입니다."));
        }
        if (checkBlank(row.leaderEmail())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀장 이메일은 필수입니다."));
        }
    }

    private void validateMemberCount(final TeamBulkRowDto row, final int rowNum, final List<TeamBulkError> errors) {
        if (row.memberNames().size() != row.memberStudentIds().size() || row.memberNames().size() != row.memberEmails().size()) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 팀원 이름, 학번, 이메일의 개수가 일치하지 않습니다."));
        }
    }

    private void validateStudentIdAndEmail(final String studentId, final String email,
                                            final int rowNum, final Set<String> seenStudentIds,
                                            final Set<String> seenEmails, final List<TeamBulkError> errors) {
        if (!checkBlank(studentId)) {
            if (!studentId.matches("\\d{7,9}")) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 학번 형식이 올바르지 않습니다."));
            }
            if (!seenStudentIds.add(studentId)) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 학번 " + studentId + "가 파일 내에서 중복됩니다."));
            }
        }

        if (!checkBlank(email)) {
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
        final List<Team> existingTeams = teamConvenience.getTeamsOfContest(contestId);

        final Set<String> existingTeamNames = existingTeams.stream()
                .map(Team::getTeamName)
                .collect(toSet());

        final Set<Long> existingMemberIds = existingTeams.stream()
                .flatMap(team -> team.getTeamMembers().stream())
                .map(TeamMember::getMemberId)
                .collect(toSet());

        for (final TeamBulkRowDto row : rows) {
            if (existingTeamNames.contains(row.teamName())) {
                errors.add(new TeamBulkError(row.rowNumber(), row.rowNumber() + "번째 행: 팀 이름 '" + row.teamName() + "'이 해당 대회에 이미 존재합니다."));
            }

            checkDuplicateMemberInContest(row.leaderEmail(), row.rowNumber(), row.leaderStudentId(), existingMemberIds, errors);
            checkStudentIdConflict(row.leaderEmail(), row.rowNumber(), row.leaderStudentId(), errors);

            for (int i = 0; i < row.memberEmails().size(); i++) {
                final String memberEmail = row.memberEmails().get(i);
                final String memberStudentId = row.memberStudentIds().get(i);
                checkDuplicateMemberInContest(memberEmail, row.rowNumber(), memberStudentId, existingMemberIds, errors);
                checkStudentIdConflict(memberEmail, row.rowNumber(), memberStudentId, errors);
            }
        }
    }

    private void checkDuplicateMemberInContest(final String email, final int rowNum, final String studentId, final Set<Long> existingMemberIds, final List<TeamBulkError> errors) {
        memberConvenience.findByEmail(email).ifPresent(member -> {
            if (existingMemberIds.contains(member.getId())) {
                errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: " + studentId + " 학생이 해당 대회의 다른 팀에 이미 소속되어 있습니다."));
            }
        });
    }

    private void checkStudentIdConflict(final String email, final int rowNum, final String studentId, final List<TeamBulkError> errors) {
        if (!checkBlank(studentId)) {
            memberConvenience.findByStudentId(studentId).ifPresent(member -> {
                if (!email.equals(member.getEmail())) {
                    errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 학번 " + studentId + "이 다른 이메일로 이미 등록되어 있습니다."));
                }
            });
        }
    }

    private boolean checkBlank(final String value) {
        return value == null || value.isBlank();
    }
}
