package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {
    Optional<DocumentEntity> findByNumber(String documentNumber);

    Optional<DocumentEntity> findBySourceUrl(String docUrl);
}
