package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.dto.UserDto;
import by.masnhyuk.lawAgent.entity.Users;
import by.masnhyuk.lawAgent.service.UserService;
import lombok.AllArgsConstructor;
import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@AllArgsConstructor
@RestController
@RequestMapping("/lawAgent")
public class UserController {

    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        userService.register(userDto);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public String login(@RequestBody UserDto user){
        System.out.println(user);
        return userService.verify(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long userId){
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> userDtos = userService.getAllUsers();
        return ResponseEntity.ok(userDtos);
    }
}
