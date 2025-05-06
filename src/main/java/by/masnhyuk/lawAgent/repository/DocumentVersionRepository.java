package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentRepository, UUID> {
    List<DocumentVersion> findByDocumentIdOrderByCreatedAtDesc(UUID documentId);
    Optional<DocumentVersion> findByDocumentAndHash(DocumentEntity document, String hash);
    void save(DocumentVersion documentVersion);
}
