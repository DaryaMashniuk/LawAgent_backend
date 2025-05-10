package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, UUID> {
    @Query("SELECT CASE WHEN COUNT(v) > 0 THEN true ELSE false END " +
            "FROM DocumentVersion v WHERE v.document = :doc AND v.contentHash = :hash")
    boolean existsByDocumentAndContentHash(@Param("doc") DocumentEntity doc,
                                           @Param("hash") String hash);

    @EntityGraph(attributePaths = "document")
    List<DocumentVersion> findByDocumentNumber(String number);
}