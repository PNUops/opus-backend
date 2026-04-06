package com.opus.opus.global.util;

import static com.opus.opus.modules.contest.exception.ContestExceptionType.EMPTY_TEAM_DATA;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.FILE_REQUIRED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.FILE_SIZE_EXCEEDED;
import static com.opus.opus.modules.contest.exception.ContestExceptionType.INVALID_FILE_FORMAT;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.opus.opus.modules.contest.application.dto.request.TeamBulkRowDto;
import com.opus.opus.modules.contest.application.dto.response.TeamBulkErrorResponse.TeamBulkError;
import com.opus.opus.modules.contest.exception.ContestException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.convenience.TeamMemberConvenience;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
@RequiredArgsConstructor
public class ExcelTeamValidator {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final TeamConvenience teamConvenience;
    private final TeamMemberConvenience teamMemberConvenience;
    private final MemberConvenience memberConvenience;

    public void validateFile(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ContestException(FILE_REQUIRED);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ContestException(FILE_SIZE_EXCEEDED);
        }
        final String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new ContestException(INVALID_FILE_FORMAT);
        }
    }

    public void validateNotEmpty(final List<TeamBulkRowDto> rows) {
        if (rows.isEmpty()) {
            throw new ContestException(EMPTY_TEAM_DATA);
        }
    }

    public List<TeamBulkError> validateRows(final List<TeamBulkRowDto> rows, final Long contestId, final Set<Integer> skippedRowNumbers) {
        final List<TeamBulkError> errors = new ArrayList<>();

        validateRowFormat(rows, errors);

        if (errors.isEmpty()) {
            validateDuplicate(rows, contestId, errors, skippedRowNumbers);
        }

        return errors;
    }

    private void validateRowFormat(final List<TeamBulkRowDto> rows, final List<TeamBulkError> errors) {
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

    private void validateDuplicate(final List<TeamBulkRowDto> rows, final Long contestId,
                                    final List<TeamBulkError> errors, final Set<Integer> skippedRowNumbers) {
        final List<Team> existingTeams = teamConvenience.getTeamsOfContest(contestId);
        final Map<String, Team> existingTeamsByName = existingTeams.stream()
                .collect(toMap(Team::getTeamName, t -> t, (a, b) -> a));

        for (final TeamBulkRowDto row : rows) {
            final Team existingTeam = existingTeamsByName.get(row.teamName());
            if (existingTeam != null && isExactMatch(row, existingTeam)) {
                skippedRowNumbers.add(row.rowNumber());
            }
        }

        final List<TeamBulkRowDto> rowsToValidate = rows.stream()
                .filter(row -> !skippedRowNumbers.contains(row.rowNumber()))
                .toList();

        validateTeamNameDuplicate(rowsToValidate, existingTeams, errors);
        validateMemberDuplicate(rowsToValidate, contestId, errors);
    }

    private boolean isExactMatch(final TeamBulkRowDto row, final Team existingTeam) {
        if (!row.projectName().equals(existingTeam.getProjectName())) {
            return false;
        }

        final List<TeamMember> teamMembers = existingTeam.getTeamMembers();
        final List<Long> memberIds = teamMembers.stream().map(TeamMember::getMemberId).toList();
        final Map<Long, Member> membersById = memberConvenience.getMembersByIds(memberIds);

        return isLeaderMatch(row, teamMembers, membersById) && isMembersMatch(row, teamMembers, membersById);
    }

    private boolean isLeaderMatch(final TeamBulkRowDto row, final List<TeamMember> teamMembers, final Map<Long, Member> membersById) {
        for (final TeamMember tm : teamMembers) {
            if (!tm.getRoles().contains(TeamMemberRoleType.ROLE_팀장)) {
                continue;
            }
            final Member leader = membersById.get(tm.getMemberId());
            if (leader == null) {
                return false;
            }
            return row.leaderName().equals(leader.getName())
                    && row.leaderStudentId().equals(leader.getStudentId())
                    && row.leaderEmail().equals(leader.getEmail());
        }
        return false;
    }

    private boolean isMembersMatch(final TeamBulkRowDto row, final List<TeamMember> teamMembers, final Map<Long, Member> membersById) {
        final List<Member> members = teamMembers.stream()
                .filter(tm -> !tm.getRoles().contains(TeamMemberRoleType.ROLE_팀장))
                .map(tm -> membersById.get(tm.getMemberId()))
                .toList();

        if (members.size() != row.memberNames().size()) {
            return false;
        }

        final Set<String> existingKeys = members.stream()
                .map(m -> m.getName() + "|" + m.getStudentId() + "|" + m.getEmail())
                .collect(toSet());

        for (int i = 0; i < row.memberNames().size(); i++) {
            final String key = row.memberNames().get(i) + "|" + row.memberStudentIds().get(i) + "|" + row.memberEmails().get(i);
            if (!existingKeys.contains(key)) {
                return false;
            }
        }

        return true;
    }

    private void validateTeamNameDuplicate(final List<TeamBulkRowDto> rows, final List<Team> existingTeams,
                                            final List<TeamBulkError> errors) {
        final Set<String> existingTeamNames = existingTeams.stream()
                .map(Team::getTeamName)
                .collect(toSet());

        for (final TeamBulkRowDto row : rows) {
            if (existingTeamNames.contains(row.teamName())) {
                errors.add(new TeamBulkError(row.rowNumber(), row.rowNumber() + "번째 행: 팀 이름 '" + row.teamName() + "'이 해당 대회에 이미 존재합니다."));
            }
        }
    }

    private void validateMemberDuplicate(final List<TeamBulkRowDto> rows, final Long contestId, final List<TeamBulkError> errors) {
        final Set<Long> existingMemberIds = teamMemberConvenience.findMemberIdsByContestId(contestId);

        final Set<String> allEmails = new HashSet<>();
        final Set<String> allStudentIds = new HashSet<>();
        for (final TeamBulkRowDto row : rows) {
            if (!checkBlank(row.leaderEmail())) allEmails.add(row.leaderEmail());
            if (!checkBlank(row.leaderStudentId())) allStudentIds.add(row.leaderStudentId());
            row.memberEmails().stream().filter(e -> !checkBlank(e)).forEach(allEmails::add);
            row.memberStudentIds().stream().filter(s -> !checkBlank(s)).forEach(allStudentIds::add);
        }

        final Map<String, Member> membersByEmail = memberConvenience.findAllByEmailIn(new ArrayList<>(allEmails));
        final Map<String, Member> membersByStudentId = memberConvenience.findAllByStudentIdIn(new ArrayList<>(allStudentIds));

        for (final TeamBulkRowDto row : rows) {
            final List<String> emailList = new ArrayList<>();
            final List<String> studentIdList = new ArrayList<>();

            emailList.add(row.leaderEmail());
            studentIdList.add(row.leaderStudentId());
            emailList.addAll(row.memberEmails());
            studentIdList.addAll(row.memberStudentIds());

            for (int i = 0; i < emailList.size(); i++) {
                checkDuplicateMemberInContest(emailList.get(i), row.rowNumber(), studentIdList.get(i), existingMemberIds, membersByEmail, errors);
                checkStudentIdConflict(emailList.get(i), row.rowNumber(), studentIdList.get(i), membersByStudentId, errors);
            }
        }
    }

    private void checkDuplicateMemberInContest(final String email, final int rowNum, final String studentId,
                                                final Set<Long> existingMemberIds, final Map<String, Member> membersByEmail,
                                                final List<TeamBulkError> errors) {
        final Member member = membersByEmail.get(email);
        if (member != null && existingMemberIds.contains(member.getId())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: " + studentId + " 학생이 해당 대회의 다른 팀에 이미 소속되어 있습니다."));
        }
    }

    private void checkStudentIdConflict(final String email, final int rowNum, final String studentId,
                                         final Map<String, Member> membersByStudentId, final List<TeamBulkError> errors) {
        if (checkBlank(studentId)) {
            return;
        }
        final Member member = membersByStudentId.get(studentId);
        if (member != null && !email.equals(member.getEmail())) {
            errors.add(new TeamBulkError(rowNum, rowNum + "번째 행: 학번 " + studentId + "이 다른 이메일로 이미 등록되어 있습니다."));
        }
    }

    private boolean checkBlank(final String value) {
        return value == null || value.isBlank();
    }
}
