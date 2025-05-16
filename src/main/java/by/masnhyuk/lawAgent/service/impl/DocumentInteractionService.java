package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.FullDocumentVersionDto;
import by.masnhyuk.lawAgent.entity.*;
import by.masnhyuk.lawAgent.exception.AuthenticationFailedException;
import by.masnhyuk.lawAgent.mapper.DocumentResponseMapper;
import by.masnhyuk.lawAgent.repository.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentInteractionService {
    private final FavouriteDocumentRepository favouriteDocumentRepo;
    private final HighlightRepository highlightRepo;
    private final UserRepository userRepo;
    private final DocumentRepository documentRepo;
    private final DocumentVersionRepository documentVersionRepo;
private static final Logger logger = LogManager.getLogger();
    public DocumentVersion updateContent(Long userId, UUID versionId, String content) {
        DocumentVersion version = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found"));

        version.setContent(content);
        return documentVersionRepo.save(version);
    }

    public void deleteHighlight(Long userId, Long highlightId) {
        Highlight highlight = highlightRepo.findById(highlightId)
                .orElseThrow(() -> new ResourceNotFoundException("Highlight not found"));

        if (!highlight.getUser().getId().equals(userId)) {
            throw new AuthenticationFailedException("User not authorized");
        }

        highlightRepo.delete(highlight);
    }

    public List<Highlight> getAllHighlights(UUID versionId) {
        return highlightRepo.findByDocumentVersionId(versionId);
    }

    public Highlight createHighlight(Long userId, UUID versionId, String text, String color) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DocumentVersion version = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        Highlight highlight = new Highlight();
        highlight.setUser(user);
        highlight.setDocumentVersion(version);
        highlight.setSelectedText(text);
        highlight.setColor(color);
        highlight.setCreatedAt(LocalDateTime.now());

        return highlightRepo.save(highlight);
    }

    public List<Highlight> getUserHighlights(Long userId, UUID versionId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DocumentVersion version = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        return highlightRepo.findByUserAndDocumentVersion(user, version);
    }


    public void addToFavorites(Long userId, UUID versionId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DocumentVersion documentVersion = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        if (!favouriteDocumentRepo.existsByUserAndDocumentVersion(user, documentVersion)) {
            FavouriteDocument favDoc = new FavouriteDocument();
            favDoc.setUser(user);
            favDoc.setDocumentVersion(documentVersion);
            favDoc.setSavedAt(LocalDateTime.now());
            favouriteDocumentRepo.save(favDoc);
        }
    }

    @Transactional
    public void removeFromFavorites(Long userId, UUID versionId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DocumentVersion documentVersion = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        favouriteDocumentRepo.deleteByUserAndDocumentVersion(user, documentVersion);
    }

    public List<FullDocumentVersionDto> getFavoriteDocuments(Long userId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favouriteDocumentRepo.findByUser(user)
                .stream()
                .map(FavouriteDocument::getDocumentVersion)
                .map(DocumentResponseMapper::convertToFullVersionDto)
                .toList();
    }

    public boolean isDocumentFavorite(Users user, UUID versionId) {
        DocumentVersion documentVersion = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        return favouriteDocumentRepo.existsByUserAndDocumentVersion(user, documentVersion);
    }


}