package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.FullDocumentVersionDto;
import by.masnhyuk.lawAgent.entity.*;
import by.masnhyuk.lawAgent.mapper.DocumentResponseMapper;
import by.masnhyuk.lawAgent.repository.*;
import by.masnhyuk.lawAgent.service.DocumentInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentInteractionServiceImpl implements DocumentInteractionService {
    private final FavouriteDocumentRepository favouriteDocumentRepo;
    private final UserRepository userRepo;
    private final DocumentVersionRepository documentVersionRepo;

    @Override
    public DocumentVersion updateContent(Long userId, UUID versionId, String content) {
        DocumentVersion version = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found"));

        version.setContent(content);
        return documentVersionRepo.save(version);
    }

    @Override
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
    @Override
    public void removeFromFavorites(Long userId, UUID versionId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        DocumentVersion documentVersion = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        favouriteDocumentRepo.deleteByUserAndDocumentVersion(user, documentVersion);
    }

    @Override
    public List<FullDocumentVersionDto> getFavoriteDocuments(Long userId) {
        Users user = userRepo.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return favouriteDocumentRepo.findByUser(user)
                .stream()
                .map(FavouriteDocument::getDocumentVersion)
                .map(DocumentResponseMapper::convertToFullVersionDto)
                .toList();
    }

    @Override
    public boolean isDocumentFavorite(Users user, UUID versionId) {
        DocumentVersion documentVersion = documentVersionRepo.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));

        return favouriteDocumentRepo.existsByUserAndDocumentVersion(user, documentVersion);
    }
}