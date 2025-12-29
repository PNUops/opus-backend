package com.opus.opus.modules.team.application;

import static com.opus.opus.modules.file.domain.FileImageType.PREVIEW;
import static com.opus.opus.modules.file.domain.FileImageType.THUMBNAIL;
import static com.opus.opus.modules.file.domain.ReferenceDomainType.TEAM;
import static com.opus.opus.modules.file.exception.FileExceptionType.EXCEED_PREVIEW_LIMIT;
import static com.opus.opus.modules.file.exception.FileExceptionType.NOT_WEBP_CONVERTED;

import com.opus.opus.global.util.ExcelParserUtil;
import com.opus.opus.global.util.ExcelValidator;
import com.opus.opus.global.util.FileStorageUtil;
import com.opus.opus.modules.contest.application.convenience.ContestConvenience;
import com.opus.opus.modules.contest.domain.Contest;
import com.opus.opus.modules.file.domain.File;
import com.opus.opus.modules.file.domain.dao.FileRepository;
import com.opus.opus.modules.file.exception.FileException;
import com.opus.opus.modules.member.application.convenience.MemberConvenience;
import com.opus.opus.modules.member.domain.Member;
import com.opus.opus.modules.team.application.convenience.TeamConvenience;
import com.opus.opus.modules.team.application.dto.TeamBulkResult;
import com.opus.opus.modules.team.application.dto.TeamExcelRow;
import com.opus.opus.modules.team.application.dto.response.TeamBulkCreateResponse;
import com.opus.opus.modules.team.domain.Team;
import com.opus.opus.modules.team.domain.TeamMember;
import com.opus.opus.modules.team.domain.TeamMemberRoleType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final ContestConvenience contestConvenience;
    private final MemberConvenience memberConvenience;
    private final ExcelParserUtil teamExcelParser;
    private final ExcelValidator teamExcelValidator;


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

    public TeamBulkCreateResponse createTeamsFromExcel(final Long contestId, final MultipartFile file) {
        contestConvenience.getValidateExistContest(contestId);

        List<TeamExcelRow> excelRows = teamExcelParser.parseExcelFile(file);
        excelRows.forEach(teamExcelValidator::validate);

        List<TeamBulkResult> results = new ArrayList<>();
        int itemOrder = getNextItemOrder(contestId);

        for (TeamExcelRow excelRow : excelRows) {
            Team savedTeam = createTeamFromExcelRow(excelRow, contestId, itemOrder++);

            TeamBulkResult result = TeamBulkResult.success(
                    excelRow.rowNumber(),
                    savedTeam.getTeamName(),
                    savedTeam.getId()
            );
            results.add(result);
        }

        return TeamBulkCreateResponse.of(results);
    }

    private Team createTeamFromExcelRow(
            final TeamExcelRow excelRow,
            final Long contestId,
            final int itemOrder
    ) {
        // Team 엔티티 생성
        Team team = Team.builder()
                .teamName(excelRow.teamName())
                .projectName(excelRow.projectName())
                .contestId(contestId)
                .trackId(null) // Track 정보 없음
                .itemOrder(itemOrder)
                .build();

        Team savedTeam = teamRepository.save(team);

        // TeamMember 생성 (팀장)
        Member leader = memberConvenience.findByStudentId(excelRow.leaderStudentId());
        createTeamMember(savedTeam, leader.getId(), Set.of(TeamMemberRoleType.ROLE_팀장));

        // TeamMember 생성 (팀원들)
        if (excelRow.memberNames() != null && !excelRow.memberNames().isEmpty()) {
            for (int i = 0; i < excelRow.memberNames().size(); i++) {
                String studentId = excelRow.memberStudentIds().get(i);
                Member member = memberConvenience.findByStudentId(studentId);
                createTeamMember(savedTeam, member.getId(), Set.of(TeamMemberRoleType.ROLE_팀원));
            }
        }

        return savedTeam;
    }

    private void createTeamMember(final Team team, final Long memberId, final Set<TeamMemberRoleType> roles) {
        TeamMember teamMember = TeamMember.builder()
                .team(team)
                .memberId(memberId)
                .roles(roles)
                .build();
        teamMemberRepository.save(teamMember);
    }

    private int getNextItemOrder(final Long contestId) {
        return teamRepository.findMaxItemOrderByContestId(contestId)
                .map(max -> max + 1)
                .orElse(1);
    }
}
