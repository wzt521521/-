package com.career.platform.analytics.controller;

import com.career.platform.analytics.service.AnalyticsService;
import com.career.platform.analytics.service.OfflineAnalysisService;
import com.career.platform.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@PreAuthorize("hasAuthority('dashboard:view')")
public class DashboardController {
    private final AnalyticsService analytics;
    private final OfflineAnalysisService offlineAnalysis;

    public DashboardController(AnalyticsService analytics, OfflineAnalysisService offlineAnalysis) {
        this.analytics = analytics;
        this.offlineAnalysis = offlineAnalysis;
    }

    @PostMapping("/refresh-cache")
    public ApiResponse<Map<String, String>> refreshCache() {
        offlineAnalysis.calculateAndPersist();
        return ApiResponse.success(Map.of("status", "completed"));
    }

    @GetMapping("/all")
    public ApiResponse<Map<String, Object>> all() { return ApiResponse.success(analytics.dashboardSnapshot()); }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() { return ApiResponse.success(analytics.overview()); }

    @GetMapping("/hot-positions")
    public ApiResponse<Object> hotPositions() { return ApiResponse.success(analytics.positionsAnalysis().get("hotPositions")); }

    @GetMapping("/salary-ranking")
    public ApiResponse<Object> salaryRanking() { return ApiResponse.success(analytics.salary().get("cityComparison")); }

    @GetMapping("/skill-cloud")
    public ApiResponse<Object> skillCloud() { return ApiResponse.success(analytics.skills().get("topSkills")); }

    @GetMapping("/trend")
    public ApiResponse<Object> trend() { return ApiResponse.success(analytics.trends().get("monthly")); }
}
