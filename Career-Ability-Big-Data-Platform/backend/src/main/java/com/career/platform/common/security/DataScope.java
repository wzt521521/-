package com.career.platform.common.security;

public final class DataScope {

    private final DataScopeType type;
    private final Long userId;
    private final String college;

    private DataScope(DataScopeType type, Long userId, String college) {
        this.type = type;
        this.userId = userId;
        this.college = college;
    }

    public static DataScope all() {
        return new DataScope(DataScopeType.ALL, null, null);
    }

    public static DataScope college(String college) {
        return new DataScope(DataScopeType.COLLEGE, null, college);
    }

    public static DataScope self(Long userId) {
        return new DataScope(DataScopeType.SELF, userId, null);
    }

    public DataScopeType getType() {
        return type;
    }

    public Long getUserId() {
        return userId;
    }

    public String getCollege() {
        return college;
    }

    public boolean isAll() {
        return type == DataScopeType.ALL;
    }
}
