package org.tokomoapp.tokomo_be.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.tokomoapp.tokomo_be.model.Game;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ExcelGameReader {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelGameReader.class);
    
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
                game.setId(Long.parseLong(getStringValue(row.getCell(0))));
                game.setGameType(getStringValue(row.getCell(1)));
                game.setGameName(getStringValue(row.getCell(2)));
                game.setDownloadUrl(getStringValue(row.getCell(3)));
                game.setPassword(getStringValue(row.getCell(4)));
                game.setExtractPassword(getStringValue(row.getCell(5)));
                game.setNote(getStringValue(row.getCell(6)));
                if(game.getGameName() == null || game.getDownloadUrl() == null || game.getDownloadUrl().isEmpty()) {
                    logger.error("Download URL or Game Name is null for game: " + game.getGameName());
                    continue;
                }
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