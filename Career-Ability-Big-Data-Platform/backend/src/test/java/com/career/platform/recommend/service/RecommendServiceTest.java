package com.career.platform.recommend.service;

import com.career.platform.position.repository.PositionRepository;
import com.career.platform.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class RecommendServiceTest {

    private RecommendService service;

    @BeforeEach
    void setUp() {
        service = new RecommendService(
                mock(ProfileRepository.class),
                mock(PositionRepository.class),
                mock(CacheManager.class));
    }

    @Test
    void calculatesJaccardScoreFromSkillIntersection() {
        double score = service.calcSkillScore(
                List.of("java", "spring"),
                List.of("java", "mysql"));

        assertEquals(1.0 / 3.0, score, 0.000001);
    }

    @Test
    void returnsZeroWhenEitherSkillSetIsEmpty() {
        assertEquals(0.0, service.calcSkillScore(List.of(), List.of("java")));
        assertEquals(0.0, service.calcSkillScore(List.of("java"), List.of()));
    }

    @Test
    void normalizesCaseWhitespaceNullsAndDuplicates() {
        List<String> normalized = service.normalizeSkills(
                Arrays.asList(" Java ", "JAVA", null, "", " Spring "));

        assertEquals(List.of("java", "spring"), normalized);
    }
}
