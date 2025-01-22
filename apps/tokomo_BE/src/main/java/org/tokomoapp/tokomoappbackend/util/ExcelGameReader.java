package org.tokomoapp.tokomoappbackend.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.tokomoapp.tokomoappbackend.model.Game;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelGameReader {
    
    public static List<Game> readGamesFromExcel(String filePath) throws Exception {
        List<Game> games = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            Workbook workbook;
            
            if (filePath.endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(fis);
            } else if (filePath.endsWith(".xls")) {
                workbook = new HSSFWorkbook(fis);
            } else {
                throw new IllegalArgumentException("不支持的文件格式");
            }
            
            Sheet sheet = workbook.getSheetAt(0);
            int firstRow = 1; // 跳过标题行
            
            for (int i = firstRow; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                // 检查行是否为空
                boolean isEmptyRow = true;
                for (int j = 0; j < 6; j++) {
                    String cellValue = getStringValue(row.getCell(j));
                    if (cellValue != null && !cellValue.trim().isEmpty()) {
                        isEmptyRow = false;
                        break;
                    }
                }
                if (isEmptyRow) continue;
                
                Game game = new Game();
                game.setId(getStringValue(row.getCell(0)));
                game.setGameType(getStringValue(row.getCell(1)));
                game.setGameName(getStringValue(row.getCell(2)));
                game.setDownloadUrl(getStringValue(row.getCell(3)));
                game.setPassword(getStringValue(row.getCell(4)));
                game.setExtractPassword(getStringValue(row.getCell(5)));
                
                games.add(game);
            }
            
            workbook.close();
        }
        
        return games;
    }
    
    private static String getStringValue(Cell cell) {
        if (cell == null) return null;
        
        String value = "";
        switch (cell.getCellType()) {
            case STRING:
                value = cell.getStringCellValue();
                break;
            case NUMERIC:
                value = String.valueOf((long)cell.getNumericCellValue());
                break;
            default:
                value = "";
        }
        
        return value.trim().isEmpty() ? null : value.trim();
    }
} 