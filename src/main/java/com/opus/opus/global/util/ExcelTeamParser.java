package com.opus.opus.global.util;

import com.opus.opus.modules.contest.application.dto.request.TeamBulkRowDto;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ExcelTeamParser {

    private static final int DATA_START_ROW = 4; // 5행부터 (0-indexed = 4)
    private static final int COL_TEAM_NAME = 0;
    private static final int COL_PROJECT_NAME = 1;
    private static final int COL_LEADER_NAME = 2;
    private static final int COL_MEMBER_NAMES = 3;
    private static final int COL_LEADER_STUDENT_ID = 4;
    private static final int COL_MEMBER_STUDENT_IDS = 5;
    private static final int COL_LEADER_EMAIL = 6;
    private static final int COL_MEMBER_EMAILS = 7;

    public List<TeamBulkRowDto> parse(final MultipartFile file) {
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            final Sheet sheet = workbook.getSheetAt(0);
            final List<TeamBulkRowDto> rows = new ArrayList<>();

            for (int i = DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
                final Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) {
                    continue;
                }

                rows.add(new TeamBulkRowDto(
                        i + 1, // 사용자에게 보여줄 1-indexed 행 번호
                        getCellStringValue(row, COL_TEAM_NAME),
                        getCellStringValue(row, COL_PROJECT_NAME),
                        getCellStringValue(row, COL_LEADER_NAME),
                        getCellStringValue(row, COL_LEADER_STUDENT_ID),
                        getCellStringValue(row, COL_LEADER_EMAIL),
                        parseCommaSeparated(getCellStringValue(row, COL_MEMBER_NAMES)),
                        parseCommaSeparated(getCellStringValue(row, COL_MEMBER_STUDENT_IDS)),
                        parseCommaSeparated(getCellStringValue(row, COL_MEMBER_EMAILS))
                ));
            }

            return rows;
        } catch (IOException e) {
            throw new IllegalArgumentException("엑셀 파일을 읽을 수 없습니다.", e);
        }
    }

    private boolean isEmptyRow(final Row row) {
        for (int i = 0; i < 8; i++) {
            final String value = getCellStringValue(row, i);
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(final Row row, final int colIndex) {
        final Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                // 학번 등 숫자가 NUMERIC으로 읽히는 경우 처리
                double numericValue = cell.getNumericCellValue();
                if (numericValue == Math.floor(numericValue)) {
                    yield String.valueOf((long) numericValue);
                }
                yield String.valueOf(numericValue);
            }
            case BLANK -> null;
            default -> cell.toString().trim();
        };
    }

    private List<String> parseCommaSeparated(final String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
