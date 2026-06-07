package com.ronin.auth;

import com.ronin.auth.dto.*;
import com.ronin.config.JwtService;
import com.ronin.ranking.RankService;
import com.ronin.users.UserEntity;
import com.ronin.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginSessionService loginSessionService;
    private final RankService rankService;
    private final WebClient.Builder webClientBuilder;

    @Value("${google.client.id:}")
    private String googleClientId;

    @Value("${google.client.secret:}")
    private String googleClientSecret;

    @Value("${google.redirect-uri:http://localhost:8080/auth/oauth2/callback/google}")
    private String googleRedirectUri;

    @Value("${frontend.oauth.success.url:http://localhost:5173/}")
    private String frontendOauthSuccessUrl;

    /**
     * Original single-step login (kept for backward compatibility)
     */
    public LoginResponse login(LoginRequest req) {
        UserEntity user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user);
        return new LoginResponse(user.getEmail(), token);
    }

    /**
     * Step 1: Verify email and preload user data in background
     */
    public EmailVerificationResponse verifyEmail(EmailVerificationRequest req) {
        log.debug("Verifying email: {}", req.getEmail());
        
        var userOpt = userRepository.findByEmail(req.getEmail());
        
        if (userOpt.isEmpty()) {
            // User doesn't exist - return userExists=false for signup
            return new EmailVerificationResponse(
                    false,
                    req.getEmail(),
                    null,
                    null,
                    null
            );
        }

        UserEntity user = userOpt.get();
        
        // Create session for this login attempt
        String sessionId = loginSessionService.createSession(user);
        
        // Get user's rank information based on completed projects
        var rankInfo = rankService.getRankForCompletedProjects(user.getCompletedProjects());
        EmailVerificationResponse.UserRankDto rankDto = new EmailVerificationResponse.UserRankDto(
                rankInfo.getName(),
                rankInfo.getBeltColor(),
                rankInfo.getLevel()
        );
        
        return new EmailVerificationResponse(
                true,
                user.getEmail(),
                user.getEmail(), // Use email as username display
                rankDto,
                sessionId
        );
    }

    /**
     * Step 2: Verify password and return JWT token
     */
    public LoginResponse verifyPassword(PasswordVerificationRequest req) {
        log.debug("Verifying password with session: {}", req.getSessionId());
        
        UserEntity user = loginSessionService.getUserFromSession(req.getSessionId());
        
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        // Invalidate session after successful login
        loginSessionService.invalidateSession(req.getSessionId());
        
        String token = jwtService.generateToken(user);
        return new LoginResponse(user.getEmail(), token);
    }

    /**
     * Signup: Create new user account and return JWT token
     */
    public SignupResponse signup(SignupRequest req) {
        log.debug("Signing up new user: {}", req.getEmail());
        
        // Check if email already exists
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create new user
        UserEntity user = new UserEntity();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setCompletedProjects(0);
        
        UserEntity savedUser = userRepository.save(user);
        
        // Generate JWT token for automatic login
        String token = jwtService.generateToken(savedUser);
        
        return new SignupResponse(
                savedUser.getEmail(),
                req.getUsername() != null ? req.getUsername() : savedUser.getEmail(),
                token,
                "Account created successfully. Welcome to Ronin!"
        );
    }

    // ----- Google OAuth helpers -----

    public String getGoogleAuthorizationUrl() {
        validateGoogleConfig();
        String scope = "openid email profile";
        String url = "https://accounts.google.com/o/oauth2/v2/auth" +
            "?client_id=" + googleClientId +
            "&response_type=code" +
            "&scope=" + scope.replace(" ", "%20") +
            "&redirect_uri=" + googleRedirectUri +
            "&access_type=offline";
        return url;
    }

    public record OAuthResult(String email, String token) {}

    public OAuthResult handleGoogleCallback(String code) {
        validateGoogleConfig();
        // Exchange code for tokens
        WebClient wc = webClientBuilder.build();

        var tokenResp = wc.post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue("code=" + code +
                        "&client_id=" + googleClientId +
                        "&client_secret=" + googleClientSecret +
                        "&redirect_uri=" + googleRedirectUri +
                        "&grant_type=authorization_code")
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .block();

        if (tokenResp == null || tokenResp.get("access_token") == null) {
            throw new RuntimeException("Failed to exchange code for token");
        }

        String accessToken = tokenResp.get("access_token").toString();

        // Fetch userinfo
        var userInfo = wc.get()
                .uri("https://openidconnect.googleapis.com/v1/userinfo")
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(java.util.Map.class)
                .block();

        if (userInfo == null || userInfo.get("email") == null) {
            throw new RuntimeException("Failed to fetch user info from Google");
        }

        String email = userInfo.get("email").toString();

        // Find or create user
        var userOpt = userRepository.findByEmail(email);
        UserEntity user;
        if (userOpt.isPresent()) {
            user = userOpt.get();
        } else {
            user = new UserEntity();
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            user.setCompletedProjects(0);
            user = userRepository.save(user);
        }

        String token = jwtService.generateToken(user);
        return new OAuthResult(email, token);
    }

    public String getFrontendOauthSuccessUrl() {
        return frontendOauthSuccessUrl;
    }

    private void validateGoogleConfig() {
        if (googleClientId == null || googleClientId.isBlank() || googleClientSecret == null || googleClientSecret.isBlank()) {
            throw new IllegalStateException("Google OAuth is not configured. Set 'google.client.id' and 'google.client.secret' in application.properties or as environment variables (GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET) and restart the backend.");
        }
    }
}

