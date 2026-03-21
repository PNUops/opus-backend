package com.opus.opus.contest;

import java.io.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.mock.web.MockMultipartFile;

public class ContestExcelFixture {
    public static MockMultipartFile createExcelFile(final String[]... dataRows) throws Exception {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            final Sheet sheet = workbook.createSheet();

            final Row headerRow = sheet.createRow(3);
            headerRow.createCell(0).setCellValue("팀 이름");
            headerRow.createCell(1).setCellValue("프로젝트 이름");
            headerRow.createCell(2).setCellValue("팀장 이름");
            headerRow.createCell(3).setCellValue("팀원 이름");
            headerRow.createCell(4).setCellValue("팀장 학번");
            headerRow.createCell(5).setCellValue("팀원 학번");
            headerRow.createCell(6).setCellValue("팀장 이메일");
            headerRow.createCell(7).setCellValue("팀원 이메일");
            for (int i = 0; i < dataRows.length; i++) {
                final Row row = sheet.createRow(4 + i);
                for (int j = 0; j < dataRows[i].length; j++) {
                    row.createCell(j).setCellValue(dataRows[i][j]);
                }
            }

            workbook.write(out);
            return new MockMultipartFile(
                    "file",
                    "teams.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    out.toByteArray()
            );
        }
    }
}
