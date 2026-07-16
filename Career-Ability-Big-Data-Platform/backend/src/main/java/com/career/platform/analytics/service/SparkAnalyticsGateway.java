package com.career.platform.analytics.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spark SQL / Hive 大数据查询网关（主通道）。
 *
 * <p>通过 JDBC 连接 Spark Thrift Server，执行聚合 SQL。
 * 所有方法失败时抛出异常，由调用方（{@link AnalyticsService} / {@link OfflineAnalysisService}）捕获后降级到 MySQL 兜底通道。
 * </p>
 */
@Component
public class SparkAnalyticsGateway {

    private static final Logger log = LoggerFactory.getLogger(SparkAnalyticsGateway.class);

    @Value("${app.analytics.spark-enabled:false}")
    private boolean sparkEnabled;

    // JdbcTemplate 指向 Spark Thrift Server 时用于大数据查询，
    // 不可用时为 null（由外部 DI 容器按 @Autowired(required=false) 处理）。
    private final JdbcTemplate sparkJdbc;

    public SparkAnalyticsGateway(@org.springframework.beans.factory.annotation.Autowired(required = false)
                                 @org.springframework.beans.factory.annotation.Qualifier("sparkJdbcTemplate")
                                 JdbcTemplate sparkJdbc) {
        this.sparkJdbc = sparkJdbc;
    }

    public boolean isEnabled() {
        return sparkEnabled && sparkJdbc != null;
    }

    // ---- 实时聚合查询（对应 AnalyticsService 各维度） ----

    public Map<String, Object> queryCity() {
        String sql = """
            SELECT city AS name, COUNT(*) AS value
            FROM job_position WHERE city IS NOT NULL
            GROUP BY city ORDER BY value DESC LIMIT 30
            """;
        List<Map<String, Object>> ranking = sparkJdbc.queryForList(sql);

        String salarySql = """
            SELECT city AS name, AVG((salary_min + salary_max) / 2.0) AS averageSalary
            FROM job_position WHERE salary_min IS NOT NULL AND city IS NOT NULL
            GROUP BY city ORDER BY averageSalary DESC LIMIT 20
            """;
        List<Map<String, Object>> salaryComparison = sparkJdbc.queryForList(salarySql);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ranking", ranking);
        result.put("salaryComparison", salaryComparison);
        result.put("heatmap", ranking);
        return result;
    }

    public Map<String, Object> querySkills() {
        String sql = """
            SELECT skill_name AS name, frequency AS value
            FROM job_skill WHERE frequency > 0
            ORDER BY frequency DESC LIMIT 30
            """;
        List<Map<String, Object>> topSkills = sparkJdbc.queryForList(sql);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("topSkills", topSkills);
        result.put("associations", List.of());
        result.put("totalTaggedPositions",
                sparkJdbc.queryForObject("SELECT COUNT(*) FROM job_position WHERE skills IS NOT NULL", Long.class));
        return result;
    }

    public Map<String, Object> queryEducation() {
        String sql = """
            SELECT education AS name, COUNT(*) AS value
            FROM job_position WHERE education IS NOT NULL
            GROUP BY education ORDER BY value DESC LIMIT 20
            """;
        List<Map<String, Object>> distribution = sparkJdbc.queryForList(sql);

        String salarySql = """
            SELECT education AS name, AVG((salary_min + salary_max) / 2.0) AS averageSalary
            FROM job_position WHERE salary_min IS NOT NULL AND education IS NOT NULL
            GROUP BY education ORDER BY averageSalary DESC
            """;
        List<Map<String, Object>> salaryComparison = sparkJdbc.queryForList(salarySql);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("distribution", distribution);
        result.put("salaryComparison", salaryComparison);
        return result;
    }

    public Map<String, Object> queryOverview() {
        String totalSql = "SELECT COUNT(*) FROM job_position";
        long total = nullableLong(sparkJdbc.queryForObject(totalSql, Long.class));

        String monthSql = "SELECT COUNT(*) FROM job_position WHERE publish_date >= ?";
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        long newThisMonth = nullableLong(sparkJdbc.queryForObject(monthSql, Long.class, java.sql.Date.valueOf(firstDay)));

        String salarySql = "SELECT AVG((salary_min + salary_max) / 2.0) FROM job_position WHERE salary_min IS NOT NULL";
        double avgSalary = nullableDouble(sparkJdbc.queryForObject(salarySql, Double.class));

        String companySql = "SELECT COUNT(DISTINCT company_id) FROM job_position WHERE company_id IS NOT NULL";
        long companies = nullableLong(sparkJdbc.queryForObject(companySql, Long.class));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalPositions", total);
        result.put("newThisMonth", newThisMonth);
        result.put("averageSalary", Math.round(avgSalary * 100.0) / 100.0);
        result.put("activeCompanies", companies);
        return result;
    }

    // ---- 离线批处理（写入 stat_* 表，替代 OfflineAnalysisService 中的 JdbcTemplate 逐条写入） ----

    public Map<String, Object> runOfflineAggregation(LocalDate today) {
        log.info("Spark SQL 离线统计开始: {}", today);
        java.sql.Date statDate = java.sql.Date.valueOf(today);

        // 清理当日已有数据
        for (String table : List.of("stat_position", "stat_salary", "stat_education",
                "stat_skill", "stat_city", "stat_company", "stat_trend")) {
            sparkJdbc.update("DELETE FROM " + table + " WHERE stat_date = ? AND stat_type = 'DAILY'", statDate);
        }

        // 岗位统计
        sparkJdbc.update("""
            INSERT INTO stat_position (stat_date, stat_type, total_count, new_count, growth_rate)
            SELECT ?, 'DAILY',
                (SELECT COUNT(*) FROM job_position),
                (SELECT COUNT(*) FROM job_position WHERE publish_date >= ?),
                NULL
            """, statDate, statDate);

        // 薪资统计
        sparkJdbc.update("""
            INSERT INTO stat_salary (stat_date, stat_type, avg_salary, median_salary)
            SELECT ?, 'DAILY',
                AVG((salary_min + salary_max) / 2.0),
                NULL
            FROM job_position WHERE salary_min IS NOT NULL
            """, statDate);

        // 学历统计
        sparkJdbc.update("""
            INSERT INTO stat_education (stat_date, stat_type, education, position_count, avg_salary)
            SELECT ?, 'DAILY', education, COUNT(*), AVG((salary_min + salary_max) / 2.0)
            FROM job_position WHERE education IS NOT NULL AND salary_min IS NOT NULL
            GROUP BY education
            """, statDate);

        // 技能统计
        sparkJdbc.update("""
            INSERT INTO stat_skill (stat_date, stat_type, skill_name, frequency, trend)
            SELECT ?, 'DAILY', skill_name, frequency, 'stable'
            FROM job_skill WHERE frequency > 0
            """, statDate);

        // 城市统计
        sparkJdbc.update("""
            INSERT INTO stat_city (stat_date, stat_type, city, province, position_count, avg_salary, rank_num)
            SELECT ?, 'DAILY', city, COALESCE(province, '未知'), COUNT(*),
                AVG((salary_min + salary_max) / 2.0),
                ROW_NUMBER() OVER (ORDER BY COUNT(*) DESC)
            FROM job_position WHERE city IS NOT NULL AND salary_min IS NOT NULL
            GROUP BY city, province
            """, statDate);

        // 企业统计：按行业
        sparkJdbc.update("""
            INSERT INTO stat_company (stat_date, stat_type, industry, company_count, position_count)
            SELECT ?, 'DAILY',
                COALESCE(c.industry, '未知'),
                COUNT(DISTINCT c.id),
                COUNT(*)
            FROM job_position p
            LEFT JOIN job_company c ON p.company_id = c.id
            WHERE c.id IS NOT NULL
            GROUP BY c.industry
            """, statDate);

        log.info("Spark SQL 离线统计完成: {}", today);
        return snapshotFromSpark(today);
    }

    public Map<String, Object> snapshotFromSpark(LocalDate today) {
        // 返回与 AnalyticsService.calculateSnapshot() 相同结构的 Map，
        // 用于缓存预热 —— 这里简单返回 null 让调用方走 MySQL 兜底来预热缓存。
        // 实际生产可在这里从 stat_* 表组装。
        return null;
    }

    // ---- 工具方法 ----

    private long nullableLong(Long value) {
        return value == null ? 0L : value;
    }

    private double nullableDouble(Double value) {
        return value == null ? 0.0 : value;
    }
}
