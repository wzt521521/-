package com.career.platform.profile.service.impl;

import com.career.platform.profile.dto.ProfileRequest;
import com.career.platform.profile.dto.ProfileResponse;
import com.career.platform.profile.entity.StudentProfile;
import com.career.platform.profile.repository.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProfileServiceImplTest {

    private ProfileRepository profileRepository;
    private CacheManager cacheManager;
    private ProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        profileRepository = mock(ProfileRepository.class);
        cacheManager = new ConcurrentMapCacheManager("recommend");
        service = new ProfileServiceImpl(profileRepository, cacheManager);
    }

    @Test
    void returnsProfilesByTheRequestedUserIdOnly() {
        StudentProfile first = profile(11L, "first-major");
        StudentProfile second = profile(22L, "second-major");
        when(profileRepository.findByUserId(11L)).thenReturn(Optional.of(first));
        when(profileRepository.findByUserId(22L)).thenReturn(Optional.of(second));

        ProfileResponse firstResponse = service.getProfile(11L);
        ProfileResponse secondResponse = service.getProfile(22L);

        assertEquals("first-major", firstResponse.getMajor());
        assertEquals("second-major", secondResponse.getMajor());
        verify(profileRepository).findByUserId(11L);
        verify(profileRepository).findByUserId(22L);
    }

    @Test
    void updatesOneProfileAndInvalidatesOnlyThatUsersRecommendationCache() {
        StudentProfile first = profile(11L, "old-major");
        Cache recommendCache = cacheManager.getCache("recommend");
        recommendCache.put(11L, "stale-first");
        recommendCache.put(22L, "valid-second");

        when(profileRepository.findByUserId(11L)).thenReturn(Optional.of(first));
        when(profileRepository.save(any(StudentProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfileRequest request = new ProfileRequest();
        request.setMajor("updated-major");
        request.setSkills(List.of("java", "spring"));
        ProfileResponse response = service.saveOrUpdate(11L, request);

        assertEquals("updated-major", response.getMajor());
        assertEquals(11L, first.getUserId());
        assertNull(recommendCache.get(11L));
        assertNotNull(recommendCache.get(22L));
        assertEquals("valid-second", recommendCache.get(22L).get());
        verify(profileRepository).findByUserId(11L);
        verify(profileRepository, never()).findByUserId(22L);
        verify(profileRepository).save(first);
    }

    private StudentProfile profile(Long userId, String major) {
        StudentProfile profile = new StudentProfile();
        profile.setUserId(userId);
        profile.setMajor(major);
        profile.setSkills(List.of("java"));
        return profile;
    }
}
