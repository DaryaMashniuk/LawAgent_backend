package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.dto.ApiResponse;
import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.exception.AuthenticationFailedException;
import by.masnhyuk.lawAgent.service.LogoutService;
import by.masnhyuk.lawAgent.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@CrossOrigin
@AllArgsConstructor
@RestController
@RequestMapping("/lawAgent")
public class UserController {

    private final UserService userService;
    private final LogoutService logoutService;
    private static final Logger logger = LogManager.getLogger();

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserDto userDto) {
        return userService.register(userDto)
                .map(registeredUser -> ResponseEntity.ok(Map.of(
                        "message", "Registration successful! Please login.",
                        "username", registeredUser.getUsername(),
                        "email", registeredUser.getEmail()
                )))
                .orElseGet(() -> ResponseEntity.badRequest()
                        .body(Map.of("error", "Registration failed: invalid data")));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDto userDto) {
        try {
            String token = userService.login(userDto.getUsername(), userDto.getPassword())
                    .orElseThrow(() -> new AuthenticationFailedException("Invalid credentials"));

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(Map.of(
                            "token", token,
                            "message", "Login successful"
                    ));
        } catch (AuthenticationFailedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "error", e.getMessage(),
                            "status", HttpStatus.UNAUTHORIZED.value()
                    ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(HttpServletRequest request,
                                                       HttpServletResponse response) {
        return logoutService.logout(request, response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
}