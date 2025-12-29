package com.opus.opus.global.util;

import com.opus.opus.modules.team.application.dto.TeamExcelRow;
import com.opus.opus.modules.team.exception.TeamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import static com.opus.opus.modules.team.exception.TeamExceptionType.*;

@Slf4j
@Component
public class ExcelParserUtil {

    private static final String TARGET_SHEET_NAME = "팀 등록";
    private static final int HEADER_ROW_INDEX = 0; // 1번째 행이 헤더 (0-based index)
    private static final int DATA_START_ROW_INDEX = 1; // 2번째 행부터 데이터 (0-based index)

    // 컬럼 인덱스 (0부터 시작)
    private static final int COL_TEAM_NAME = 0;          // A열: 팀 이름
    private static final int COL_PROJECT_NAME = 1;       // B열: 프로젝트 이름
    private static final int COL_LEADER_NAME = 2;        // C열: 팀장 이름
    private static final int COL_MEMBER_NAMES = 3;       // D열: 팀원 이름
    private static final int COL_LEADER_STUDENT_ID = 4;  // E열: 팀장 학번
    private static final int COL_MEMBER_STUDENT_IDS = 5; // F열: 팀원 학번

    public List<TeamExcelRow> parseExcelFile(final MultipartFile file) {
        validateFileExtension(file);

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet(TARGET_SHEET_NAME);

            if (sheet == null) {
                throw new TeamException(INVALID_SHEET_NAME);
            }

            List<TeamExcelRow> teamRows = new ArrayList<>();

            // 데이터 행부터 마지막 행까지 읽기
            for (int i = DATA_START_ROW_INDEX; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isEmptyRow(row)) {
                    continue;
                }

                TeamExcelRow teamRow = parseRow(row, i + 1); // Excel 행번호는 1부터 시작
                teamRows.add(teamRow);
            }

            if (teamRows.isEmpty()) {
                throw new TeamException(EMPTY_EXCEL_FILE);
            }

            return teamRows;

        } catch (IOException e) {
            log.error("Excel 파일 파싱 중 오류 발생", e);
            throw new TeamException(EXCEL_PARSE_ERROR);
        }
    }

    private void validateFileExtension(final MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new TeamException(FILE_REQUIRED);
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new TeamException(INVALID_FILE_EXTENSION);
        }
    }

    private boolean isEmptyRow(final Row row) {
        if (row == null) {
            return true;
        }

        // 팀 이름(첫 번째 컬럼)이 비어있으면 빈 행으로 간주
        Cell firstCell = row.getCell(COL_TEAM_NAME);
        if (firstCell == null || firstCell.getCellType() == CellType.BLANK) {
            return true;
        }

        String value = getCellValueAsString(firstCell);
        return value == null || value.trim().isEmpty();
    }

    private TeamExcelRow parseRow(final Row row, final int excelRowNumber) {
        String teamName = getCellValueAsString(row.getCell(COL_TEAM_NAME));
        String projectName = getCellValueAsString(row.getCell(COL_PROJECT_NAME));
        String leaderName = getCellValueAsString(row.getCell(COL_LEADER_NAME));
        String leaderStudentId = getCellValueAsString(row.getCell(COL_LEADER_STUDENT_ID));
        String memberNamesStr = getCellValueAsString(row.getCell(COL_MEMBER_NAMES));
        String memberStudentIdsStr = getCellValueAsString(row.getCell(COL_MEMBER_STUDENT_IDS));

        List<String> memberNames = parseDelimitedString(memberNamesStr);
        List<String> memberStudentIds = parseDelimitedString(memberStudentIdsStr);

        return TeamExcelRow.builder()
                .rowNumber(excelRowNumber)
                .teamName(teamName)
                .projectName(projectName)
                .leaderName(leaderName)
                .leaderStudentId(leaderStudentId)
                .memberNames(memberNames)
                .memberStudentIds(memberStudentIds)
                .build();
    }

    private String getCellValueAsString(final Cell cell) {
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                // 학번 같은 숫자는 소수점 없이 정수로 변환
                double numericValue = cell.getNumericCellValue();
                if (numericValue == (long) numericValue) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue().trim();
                } catch (IllegalStateException e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> null;
        };
    }

    private List<String> parseDelimitedString(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}

