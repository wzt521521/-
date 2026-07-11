package com.career.platform.auth.dto;

import java.util.List;

public class UserImportResult {

    private final int totalRows;
    private final int importedRows;
    private final int failedRows;
    private final List<UserImportError> errors;

    public UserImportResult(
            int totalRows,
            int importedRows,
            List<UserImportError> errors) {
        this.totalRows = totalRows;
        this.importedRows = importedRows;
        this.failedRows = errors.size();
        this.errors = List.copyOf(errors);
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getImportedRows() {
        return importedRows;
    }

    public int getFailedRows() {
        return failedRows;
    }

    public List<UserImportError> getErrors() {
        return errors;
    }
}
