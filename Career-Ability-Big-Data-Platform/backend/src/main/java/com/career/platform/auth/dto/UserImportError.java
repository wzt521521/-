package com.career.platform.auth.dto;

public class UserImportError {

    private final int row;
    private final String username;
    private final String message;

    public UserImportError(int row, String username, String message) {
        this.row = row;
        this.username = username;
        this.message = message;
    }

    public int getRow() {
        return row;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
