package com.career.platform.auth.service;

import com.career.platform.auth.dto.UserImportResult;
import java.io.InputStream;

public interface UserImportService {
    UserImportResult importUsers(InputStream inputStream);

    byte[] createTemplate();
}
