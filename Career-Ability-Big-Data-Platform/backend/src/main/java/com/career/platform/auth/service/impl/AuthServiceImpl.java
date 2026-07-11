package com.career.platform.auth.service.impl;

import com.career.platform.auth.dto.ChangePasswordRequest;
import com.career.platform.auth.dto.LoginRequest;
import com.career.platform.auth.dto.RegisterRequest;
import com.career.platform.auth.dto.TokenResponse;
import com.career.platform.auth.dto.UpdateProfileRequest;
import com.career.platform.auth.dto.UserResponse;
import com.career.platform.auth.entity.SysRole;
import com.career.platform.auth.entity.SysUser;
import com.career.platform.auth.repository.SysRoleRepository;
import com.career.platform.auth.repository.SysUserRepository;
import com.career.platform.auth.security.CustomUserPrincipal;
import com.career.platform.auth.security.IssuedToken;
import com.career.platform.auth.security.JwtTokenProvider;
import com.career.platform.auth.security.LoginAttemptService;
import com.career.platform.auth.security.TokenClaims;
import com.career.platform.auth.security.TokenStore;
import com.career.platform.auth.service.AuthService;
import com.career.platform.common.error.BusinessException;
import com.career.platform.common.error.ErrorCode;
import com.career.platform.common.security.CurrentUser;
import com.career.platform.common.security.CurrentUserProvider;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE = "ROLE_STUDENT";

    private final SysUserRepository userRepository;
    private final SysRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TokenStore tokenStore;
    private final LoginAttemptService loginAttemptService;
    private final CurrentUserProvider currentUserProvider;

    public AuthServiceImpl(
            SysUserRepository userRepository,
            SysRoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            TokenStore tokenStore,
            LoginAttemptService loginAttemptService,
            CurrentUserProvider currentUserProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.tokenStore = tokenStore;
        this.loginAttemptService = loginAttemptService;
        this.currentUserProvider = currentUserProvider;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }
        String roleCode = StringUtils.hasText(request.getRoleCode())
                ? request.getRoleCode()
                : DEFAULT_ROLE;
        if (!"ROLE_STUDENT".equals(roleCode) && !"ROLE_TEACHER".equals(roleCode)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Role is not available for self-registration");
        }
        SysRole role = roleRepository.findByRoleCode(roleCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR, "Default role is not configured"));
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setRoles(Collections.singleton(role));
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        if (loginAttemptService.isBlocked(request.getUsername())) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "Account is temporarily locked");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));
            loginAttemptService.recordSuccess(request.getUsername());
            CustomUserPrincipal principal = (CustomUserPrincipal) authentication.getPrincipal();
            SysUser user = requireActiveUser(principal.getId());
            return issueTokenPair(user);
        } catch (BadCredentialsException | DisabledException exception) {
            loginAttemptService.recordFailure(request.getUsername());
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Invalid username or password");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        TokenClaims claims = tokenProvider.parseRefreshToken(refreshToken);
        if (!tokenStore.consumeRefreshToken(claims.getTokenId(), claims.getUserId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Refresh token has been revoked");
        }
        SysUser user = requireActiveUser(claims.getUserId());
        return issueTokenPair(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return UserResponse.from(requireActiveUser(currentUserProvider.requireCurrentUser().getId()));
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UpdateProfileRequest request) {
        CurrentUser currentUser = currentUserProvider.requireCurrentUser();
        SysUser user = requireActiveUser(currentUser.getId());
        user.setRealName(request.getRealName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        SysUser user = requireActiveUser(currentUserProvider.requireCurrentUser().getId());
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Current password is incorrect");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "New password must be different");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        TokenClaims accessClaims = tokenProvider.parseAccessToken(accessToken);
        tokenStore.blacklistAccessToken(
                accessClaims.getTokenId(),
                tokenProvider.remainingValidity(accessClaims));
        if (StringUtils.hasText(refreshToken)) {
            TokenClaims refreshClaims = tokenProvider.parseRefreshToken(refreshToken);
            if (accessClaims.getUserId().equals(refreshClaims.getUserId())) {
                tokenStore.revokeRefreshToken(refreshClaims.getTokenId());
            }
        }
    }

    private TokenResponse issueTokenPair(SysUser user) {
        IssuedToken accessToken = tokenProvider.createAccessToken(user.getId(), user.getUsername());
        IssuedToken refreshToken = tokenProvider.createRefreshToken(user.getId(), user.getUsername());
        Duration refreshTtl = Duration.between(Instant.now(), refreshToken.getExpiresAt());
        tokenStore.storeRefreshToken(refreshToken.getId(), user.getId(), refreshTtl);
        return new TokenResponse(
                accessToken.getValue(),
                refreshToken.getValue(),
                tokenProvider.getAccessTokenTtl().getSeconds(),
                UserResponse.from(user));
    }

    private SysUser requireActiveUser(Long userId) {
        SysUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "User not found"));
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "User is disabled");
        }
        return user;
    }
}
