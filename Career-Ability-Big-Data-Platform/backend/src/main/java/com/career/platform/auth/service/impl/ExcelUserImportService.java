package com.career.platform.auth.service.impl;

import com.career.platform.auth.dto.AdminCreateUserRequest;
import com.career.platform.auth.dto.UserImportError;
import com.career.platform.auth.dto.UserImportResult;
import com.career.platform.auth.service.UserImportService;
import com.career.platform.auth.service.UserManagementService;
import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ExcelUserImportService implements UserImportService {

    private static final int MAX_ROWS = 1000;
    private static final List<String> HEADERS = List.of(
            "username",
            "password",
            "realName",
            "email",
            "phone",
            "college",
            "roleCodes");
    private static final Set<String> REQUIRED_HEADERS = Set.of("username", "password", "rolecodes");

    private final UserManagementService userManagementService;
    private final Validator validator;

    public ExcelUserImportService(
            UserManagementService userManagementService,
            Validator validator) {
        this.userManagementService = userManagementService;
        this.validator = validator;
    }

    @Override
    public UserImportResult importUsers(InputStream inputStream) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(inputStream)) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Excel workbook has no worksheet");
            }
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getLastRowNum() > MAX_ROWS) {
                throw new BusinessException(
                        ErrorCode.BAD_REQUEST,
                        "Excel workbook may contain at most " + MAX_ROWS + " data rows");
            }

            DataFormatter formatter = new DataFormatter();
            Map<String, Integer> columns = readColumns(sheet.getRow(0), formatter);
            Set<String> usernames = new LinkedHashSet<>();
            List<UserImportError> errors = new java.util.ArrayList<>();
            int totalRows = 0;
            int importedRows = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (isEmpty(row, columns.values(), formatter)) {
                    continue;
                }
                totalRows++;
                AdminCreateUserRequest request = toRequest(row, columns, formatter);
                int excelRow = rowIndex + 1;
                String username = request.getUsername();

                String validationMessage = validate(request);
                if (validationMessage != null) {
                    errors.add(new UserImportError(excelRow, username, validationMessage));
                    continue;
                }
                if (!usernames.add(username)) {
                    errors.add(new UserImportError(excelRow, username, "Duplicate username in workbook"));
                    continue;
                }

                try {
                    userManagementService.create(request);
                    importedRows++;
                } catch (BusinessException exception) {
                    errors.add(new UserImportError(excelRow, username, exception.getMessage()));
                }
            }
            return new UserImportResult(totalRows, importedRows, errors);
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Invalid .xlsx workbook");
        }
    }

    @Override
    public byte[] createTemplate() {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("users");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row header = sheet.createRow(0);
            for (int index = 0; index < HEADERS.size(); index++) {
                Cell cell = header.createCell(index);
                cell.setCellValue(HEADERS.get(index));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(index, index < 2 ? 18 * 256 : 22 * 256);
            }

            Row sample = sheet.createRow(1);
            String[] values = {
                "student01",
                "student123",
                "示例学生",
                "student01@example.com",
                "13800000000",
                "计算机学院",
                "ROLE_STUDENT"
            };
            for (int index = 0; index < values.length; index++) {
                sample.createCell(index).setCellValue(values[index]);
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Unable to create import template");
        }
    }

    private Map<String, Integer> readColumns(Row header, DataFormatter formatter) {
        if (header == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Excel header row is required");
        }
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (Cell cell : header) {
            String name = formatter.formatCellValue(cell).trim().toLowerCase();
            if (!name.isEmpty()) {
                columns.put(name, cell.getColumnIndex());
            }
        }
        Set<String> missing = REQUIRED_HEADERS.stream()
                .filter(name -> !columns.containsKey(name))
                .collect(Collectors.toCollection(TreeSet::new));
        if (!missing.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    "Missing required Excel columns: " + String.join(", ", missing));
        }
        return columns;
    }

    private AdminCreateUserRequest toRequest(
            Row row,
            Map<String, Integer> columns,
            DataFormatter formatter) {
        AdminCreateUserRequest request = new AdminCreateUserRequest();
        request.setUsername(value(row, columns, "username", formatter));
        request.setPassword(value(row, columns, "password", formatter));
        request.setRealName(value(row, columns, "realname", formatter));
        request.setEmail(value(row, columns, "email", formatter));
        request.setPhone(value(row, columns, "phone", formatter));
        request.setCollege(value(row, columns, "college", formatter));
        String roleCodes = value(row, columns, "rolecodes", formatter);
        request.setRoleCodes(Arrays.stream(roleCodes.split("[,;，；\\s]+"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        return request;
    }

    private String validate(AdminCreateUserRequest request) {
        Set<ConstraintViolation<AdminCreateUserRequest>> violations = validator.validate(request);
        if (violations.isEmpty()) {
            return null;
        }
        return violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .sorted()
                .collect(Collectors.joining("; "));
    }

    private boolean isEmpty(
            Row row,
            java.util.Collection<Integer> indexes,
            DataFormatter formatter) {
        if (row == null) {
            return true;
        }
        return indexes.stream().allMatch(index -> formatter
                .formatCellValue(row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL))
                .trim()
                .isEmpty());
    }

    private String value(
            Row row,
            Map<String, Integer> columns,
            String name,
            DataFormatter formatter) {
        Integer index = columns.get(name);
        if (index == null || row == null) {
            return "";
        }
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
