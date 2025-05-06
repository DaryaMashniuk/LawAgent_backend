package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;

public interface LogoutService {
    ResponseEntity<ApiResponse<Boolean>> logout(HttpServletRequest request, HttpServletResponse response);
}