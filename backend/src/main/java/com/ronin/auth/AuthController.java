package com.ronin.auth;

import com.ronin.auth.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Original single-step login (backward compatibility)
     */
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    /**
     * Step 1: Verify email and preload user data
     * POST /auth/verify-email
     */
    @PostMapping("/verify-email")
    public EmailVerificationResponse verifyEmail(@RequestBody EmailVerificationRequest req) {
        return authService.verifyEmail(req);
    }

    /**
     * Step 2: Verify password and return JWT token
     * POST /auth/verify-password
     */
    @PostMapping("/verify-password")
    public LoginResponse verifyPassword(@RequestBody PasswordVerificationRequest req) {
        return authService.verifyPassword(req);
    }

    /**
     * Signup: Create new user account
     * POST /auth/signup
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignupResponse signup(@RequestBody SignupRequest req) {
        return authService.signup(req);
    }

    /**
     * Redirects the user to Google's OAuth2 consent page.
     */
    @GetMapping("/oauth2/authorize/google")
    public RedirectView authorizeGoogle() {
        String url = authService.getGoogleAuthorizationUrl();
        return new RedirectView(url);
    }

    /**
     * OAuth2 callback endpoint Google will redirect back to with `code`.
     * Exchanges code for token, creates/loads user and redirects to frontend with JWT.
     */
    @GetMapping("/oauth2/callback/google")
    public RedirectView googleCallback(@RequestParam("code") String code) {
        var result = authService.handleGoogleCallback(code);
        // Redirect frontend with token and email in query params
        String frontend = authService.getFrontendOauthSuccessUrl();
        String redirect = String.format("%s?token=%s&email=%s", frontend, result.token(), result.email());
        return new RedirectView(redirect);
    }
}
