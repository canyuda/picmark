package com.example.demo;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles Excel file operations for importing and exporting annotations.
 */
public class ExcelHandler {

    /**
     * Save annotations to Excel file.
     * Format: (x1,y1), (x2,y2), (x3,y3), (x4,y4), metadata
     */
    public static void saveAnnotations(List<RectangleAnnotation> annotations,
                                       String imageName,
                                       File outputFile) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Annotations");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"(x1,y1)", "(x2,y2)", "(x3,y3)", "(x4,y4)", "metadata"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);

                // Style header
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Add data rows
            int rowNum = 1;
            for (RectangleAnnotation annotation : annotations) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(String.format("(%.2f,%.2f)", annotation.getX1(), annotation.getY1()));
                row.createCell(1).setCellValue(String.format("(%.2f,%.2f)", annotation.getX2(), annotation.getY2()));
                row.createCell(2).setCellValue(String.format("(%.2f,%.2f)", annotation.getX3(), annotation.getY3()));
                row.createCell(3).setCellValue(String.format("(%.2f,%.2f)", annotation.getX4(), annotation.getY4()));
                row.createCell(4).setCellValue(annotation.getMetadata());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
                workbook.write(fileOut);
            }
        }

        System.out.println("Annotations saved to: " + outputFile.getAbsolutePath());
    }
    
    /**
     * Load annotations from Excel file.
     * Format: (x1,y1), (x2,y2), (x3,y3), (x4,y4), metadata
     */
    public static List<RectangleAnnotation> loadAnnotations(File excelFile) throws IOException {
        List<RectangleAnnotation> annotations = new ArrayList<>();

        try (FileInputStream fileIn = new FileInputStream(excelFile);
             Workbook workbook = WorkbookFactory.create(fileIn)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Skip header row, start from row 1
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                RectangleAnnotation annotation = new RectangleAnnotation();

                // Parse coordinate strings like "(123.45,678.90)"
                String[] coords = parseCoordinate(getStringCellValue(row.getCell(0)));
                if (coords != null) {
                    annotation.setX1(Double.parseDouble(coords[0]));
                    annotation.setY1(Double.parseDouble(coords[1]));
                }

                coords = parseCoordinate(getStringCellValue(row.getCell(1)));
                if (coords != null) {
                    annotation.setX2(Double.parseDouble(coords[0]));
                    annotation.setY2(Double.parseDouble(coords[1]));
                }

                coords = parseCoordinate(getStringCellValue(row.getCell(2)));
                if (coords != null) {
                    annotation.setX3(Double.parseDouble(coords[0]));
                    annotation.setY3(Double.parseDouble(coords[1]));
                }

                coords = parseCoordinate(getStringCellValue(row.getCell(3)));
                if (coords != null) {
                    annotation.setX4(Double.parseDouble(coords[0]));
                    annotation.setY4(Double.parseDouble(coords[1]));
                }

                Cell metadataCell = row.getCell(4);
                if (metadataCell != null) {
                    annotation.setMetadata(getStringCellValue(metadataCell));
                }

                annotations.add(annotation);
            }
        }

        System.out.println("Loaded " + annotations.size() + " annotations from: " + excelFile.getAbsolutePath());
        return annotations;
    }
    
    private static double getNumericCellValue(Cell cell) {
        if (cell == null) return 0;
        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            default:
                return 0;
        }
    }
    
    private static String getStringCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            default:
                return "";
        }
    }

    /**
     * Parse coordinate string in format "(x,y)" and return array [x, y]
     */
    private static String[] parseCoordinate(String coordStr) {
        if (coordStr == null || coordStr.isEmpty()) {
            return null;
        }

        // Remove parentheses and split by comma
        String trimmed = coordStr.trim();
        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
            String[] parts = trimmed.split(",");
            if (parts.length == 2) {
                return parts;
            }
        }

        return null;
    }
}
