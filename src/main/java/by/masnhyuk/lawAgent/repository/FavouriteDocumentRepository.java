package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.entity.FavouriteDocument;
import by.masnhyuk.lawAgent.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

public interface FavouriteDocumentRepository extends JpaRepository<FavouriteDocument, Long> {
    List<FavouriteDocument> findByUser(Users user);

    boolean existsByUserAndDocumentVersion(Users user, DocumentVersion document);

    void deleteByUserAndDocumentVersion(Users user, DocumentVersion document);
}