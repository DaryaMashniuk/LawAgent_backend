package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.FullDocumentVersionDto;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.entity.Users;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface DocumentInteractionService {
    DocumentVersion updateContent(Long userId, UUID versionId, String content);

    void addToFavorites(Long userId, UUID versionId);

    @Transactional
    void removeFromFavorites(Long userId, UUID versionId);

    List<FullDocumentVersionDto> getFavoriteDocuments(Long userId);

    boolean isDocumentFavorite(Users user, UUID versionId);
}
