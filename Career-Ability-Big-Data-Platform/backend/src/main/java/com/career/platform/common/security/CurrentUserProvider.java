package com.career.platform.common.security;

public interface CurrentUserProvider {
    CurrentUser requireCurrentUser();
}
