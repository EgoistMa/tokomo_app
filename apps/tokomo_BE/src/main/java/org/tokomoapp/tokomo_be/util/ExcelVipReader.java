package org.tokomoapp.tokomo_be.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tokomoapp.tokomo_be.model.VipCode;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExcelVipReader {
    
    private static final Logger logger = LoggerFactory.getLogger(ExcelVipReader.class);
    
    public static List<VipCode> readVipCodesFromExcel(String filePath) throws Exception {
        if (filePath.endsWith(".csv")) {
            return readFromCsv(filePath);
        } else {
            return readFromExcel(filePath);
        }
    }
    
    private static List<VipCode> readFromCsv(String filePath) throws Exception {
        List<VipCode> codes = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                String[] values = line.split(",");
                if (values.length < 2) {
                    logger.warn("跳过无效行: " + line);
                    continue;
                }
                
                String code = values[0].trim();
                Integer validDays = null;
                try {
                    validDays = Integer.parseInt(values[1].trim());
                } catch (NumberFormatException e) {
                    logger.warn("跳过无效行（天数格式错误）: " + line);
                    continue;
                }
                
                VipCode vipCode = new VipCode();
                vipCode.setCode(code);
                vipCode.setValidDays(validDays);
                
                // 读取状态
                if (values.length > 2) {
                    vipCode.setUsed("已使用".equals(values[2].trim()));
                }
                
                // 读取使用者ID
                if (values.length > 3 && !values[3].trim().isEmpty()) {
                    try {
                        vipCode.setUsedBy(Long.parseLong(values[3].trim()));
                    } catch (NumberFormatException e) {
                        logger.warn("使用者ID格式错误: " + values[3]);
                    }
                }
                
                // 读取使用时间
                if (values.length > 4 && !values[4].trim().isEmpty()) {
                    LocalDateTime usedAt = parseDateString(values[4].trim());
                    if (usedAt != null) {
                        vipCode.setUsedAt(usedAt);
                    } else {
                        logger.warn("使用时间格式错误: " + values[4]);
                    }
                }
                
                codes.add(vipCode);
            }
        }
        
        return codes;
    }
    
    private static List<VipCode> readFromExcel(String filePath) throws Exception {
        List<VipCode> codes = new ArrayList<>();
        
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
                
                Cell[] cells = new Cell[5];
                for (int j = 0; j < 5; j++) {
                    cells[j] = row.getCell(j);
                }
                
                String code = cells[0] != null ? cells[0].getStringCellValue().trim() : null;
                Integer validDays = cells[1] != null ? (int)cells[1].getNumericCellValue() : null;
                String status = cells[2] != null ? cells[2].getStringCellValue().trim() : null;
                String usedByStr = cells[3] != null ? cells[3].getStringCellValue().trim() : null;
                String dateStr = null;
                if (cells[4] != null) {
                    if (cells[4].getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cells[4])) {
                        dateStr = cells[4].getLocalDateTimeCellValue().toString().replace("T", " ");
                    } else {
                        dateStr = cells[4].getStringCellValue().trim();
                    }
                }
                
                if (code == null || validDays == null) {
                    logger.warn("跳过无效行: " + i);
                    continue;
                }
                
                VipCode vipCode = new VipCode();
                vipCode.setCode(code);
                vipCode.setValidDays(validDays);
                vipCode.setUsed("已使用".equals(status));
                
                if (usedByStr != null && !usedByStr.isEmpty()) {
                    try {
                        vipCode.setUsedBy(Long.parseLong(usedByStr));
                    } catch (NumberFormatException e) {
                        logger.warn("使用者ID格式错误: " + usedByStr);
                    }
                }
                
                if (dateStr != null && !dateStr.isEmpty()) {
                    LocalDateTime usedAt = parseDateString(dateStr);
                    if (usedAt != null) {
                        vipCode.setUsedAt(usedAt);
                    } else {
                        logger.warn("使用时间格式错误: " + dateStr);
                    }
                }
                
                codes.add(vipCode);
            }
            
            workbook.close();
        }
        
        return codes;
    }
    
    private static LocalDateTime parseDateString(String dateStr) {
        try {
            String[] parts = dateStr.split(" ");
            if (parts.length == 2) {
                String[] dateParts = parts[0].split("-");
                String[] timeParts = parts[1].split(":");
                
                int year = Integer.parseInt(dateParts[0]);
                int month = Integer.parseInt(dateParts[1]);
                int day = Integer.parseInt(dateParts[2]);
                int hour = Integer.parseInt(timeParts[0]);
                int minute = Integer.parseInt(timeParts[1]);
                int second = timeParts.length > 2 ? Integer.parseInt(timeParts[2]) : 0;
                
                return LocalDateTime.of(year, month, day, hour, minute, second);
            }
        } catch (Exception e) {
            logger.warn("日期格式解析错误: " + e.getMessage());
        }
        return null;
    }
} 