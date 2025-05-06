package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.ApiResponse;
import by.masnhyuk.lawAgent.service.LogoutService;
import by.masnhyuk.lawAgent.service.TokenBlacklistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;

@Service
public class LogoutServiceImpl implements LogoutService {
    private final TokenBlacklistService tokenBlacklistService;

    public LogoutServiceImpl(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> logout(HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            String token = extractToken(request);
            if (token != null) {
                tokenBlacklistService.addToBlacklist(token);
            }
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            return ResponseEntity.ok(ApiResponse.success(true));
        }
        return ResponseEntity.ok(ApiResponse.error("No active session found"));
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}