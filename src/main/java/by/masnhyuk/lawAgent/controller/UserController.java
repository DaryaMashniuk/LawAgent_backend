package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.dto.AuthResponse;
import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.entity.Users;
import by.masnhyuk.lawAgent.service.UserService;
import by.masnhyuk.lawAgent.service.impl.JWTService;
import lombok.AllArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin
@AllArgsConstructor
@RestController
@RequestMapping("/lawAgent")
public class UserController {

    private UserService userService;
    private JWTService jwtService;
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody UserDto userDto) {
        UserDto registeredUser = userService.register(userDto);
        String token = jwtService.generateToken(registeredUser.getUsername());
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserDto user){
        String token = userService.verify(user);
        Map<String,String> responce = new HashMap<>();
        responce.put("token",token);
        return ResponseEntity.ok(responce);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long userId){
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtos = userService.getAllUsers();
        return ResponseEntity.ok(userDtos);
    }
}
