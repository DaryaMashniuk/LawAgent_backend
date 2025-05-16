package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.entity.Highlight;
import by.masnhyuk.lawAgent.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HighlightRepository extends JpaRepository<Highlight, Long> {
    List<Highlight> findByUserAndDocumentVersion(Users user, DocumentVersion documentVersion);

    List<Highlight> findByDocumentVersionId(UUID versionId);
}