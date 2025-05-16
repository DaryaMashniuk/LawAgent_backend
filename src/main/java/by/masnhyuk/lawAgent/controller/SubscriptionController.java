package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.repository.SubscriptionService;
import by.masnhyuk.lawAgent.repository.UserRepository;
import by.masnhyuk.lawAgent.service.impl.JWTServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final JWTServiceImpl jwtService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<Void> subscribe(
            @RequestParam DocumentCategory category,
            @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        subscriptionService.subscribeUser(userId, category);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unsubscribe(
            @RequestParam DocumentCategory category,
            @RequestHeader("Authorization") String token) {
        Long userId = getUserIdFromToken(token);
        subscriptionService.unsubscribeUser(userId, category);
        return ResponseEntity.ok().build();
    }

    private Long getUserIdFromToken(String token) {
        String username = jwtService.extractUserName(token.substring(7));
        return userRepository.findByUsername(username)
                .getId();
    }
}