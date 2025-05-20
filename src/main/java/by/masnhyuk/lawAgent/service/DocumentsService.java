package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.DocumentDto;
import by.masnhyuk.lawAgent.dto.DocumentWithVersionsDto;
import by.masnhyuk.lawAgent.dto.FullDocumentVersionDto;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DocumentsService {
    List<DocumentEntity> getAllDocuments();

    Optional<DocumentDto> getDocumentById(UUID documentId);

    Page<DocumentWithVersionsDto> searchDocuments(String query, Set<DocumentCategory> categories, Pageable pageable);

    Optional<FullDocumentVersionDto> getDocumentVersionById(UUID versionId);
}
