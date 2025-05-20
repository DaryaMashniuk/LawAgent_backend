package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.dto.FullDocumentVersionDto;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.entity.Users;
import by.masnhyuk.lawAgent.exception.AuthenticationFailedException;
import by.masnhyuk.lawAgent.repository.UserRepository;
import by.masnhyuk.lawAgent.service.DocumentInteractionService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/lawAgent/documents")
@RequiredArgsConstructor
public class DocumentInteractionController {
    private final DocumentInteractionService docInteractionService;
    private final UserRepository userRepository;
    private static final Logger logger = LogManager.getLogger();

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error("User not authenticated. Authentication object: {}", authentication);
            throw new AuthenticationFailedException("User not AUTHENTICATED");
        }

        Users user = userRepository.findByUsername(authentication.getName());
        if (user == null) {
            logger.error("User not found in DB for username: {}", authentication.getName());
            throw new AuthenticationFailedException("User not FOUND");
        }

        return user;
    }


    @PostMapping("/versions/{versionId}/favorites")
    public ResponseEntity<?> addToFavorites(@PathVariable UUID versionId) {
        Users user = getCurrentUser();
        docInteractionService.addToFavorites(user.getId(), versionId);
        return ResponseEntity.ok(Map.of("status", "added"));
    }

    @DeleteMapping("/versions/{versionId}/favorites")
    public ResponseEntity<?> removeFromFavorites(@PathVariable UUID versionId) {
        Users user = getCurrentUser();
        docInteractionService.removeFromFavorites(user.getId(), versionId);
        return ResponseEntity.ok(Map.of("status", "removed"));
    }

    @GetMapping("/versions/{versionId}/favorites/check")
    public ResponseEntity<Boolean> checkIsFavorite(@PathVariable UUID versionId) {
        Users user = getCurrentUser();
        boolean isFavorite = docInteractionService.isDocumentFavorite(user, versionId);
        return ResponseEntity.ok(isFavorite);
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<FullDocumentVersionDto>> getFavoriteDocuments() {
        Users user = getCurrentUser();
        List<FullDocumentVersionDto> favorites = docInteractionService.getFavoriteDocuments(user.getId());
        return ResponseEntity.ok(favorites);
    }

    @PutMapping("/versions/{versionId}/content")
    public ResponseEntity<DocumentVersion> updateContent(
            @PathVariable UUID versionId,
            @RequestBody String content
    ) {
        Users user = getCurrentUser();
        logger.info("User!!!!!!"+user.getUsername());
        DocumentVersion updatedVersion = docInteractionService.updateContent(user.getId(), versionId, content);
        return ResponseEntity.ok(updatedVersion);
    }

}
