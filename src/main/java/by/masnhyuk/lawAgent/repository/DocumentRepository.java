package by.masnhyuk.lawAgent.repository;

import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    Optional<DocumentEntity> findBySourceUrl(String docUrl);

    Optional<DocumentEntity> findByTitle(String title);

    @Query("SELECT d FROM DocumentEntity d WHERE " +
            "d.title = :title AND " +
            "d.details LIKE CONCAT('%', :baseDescription, '%')")
    Optional<DocumentEntity> findByBaseTitleAndDetails(
            @Param("title") String title,
            @Param("baseDescription") String baseDescription);
//
//    Page<DocumentEntity> findByCategoriesIgnoreCase(String category, Pageable pageable);
//
//    Page<DocumentEntity> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
//            String title, String description, Pageable pageable
//    );
//
//    Page<DocumentEntity> findByCategoryIgnoreCaseAndTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
//            String category, String title, String description, Pageable pageable
//    );

    @Query("SELECT d FROM DocumentEntity d " +
            "LEFT JOIN d.categories c " +
            "WHERE (COALESCE(:query, '') = '' OR LOWER(d.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(d.details) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "AND (COALESCE(:categories, NULL) IS NULL OR c IN (:categories) OR d.groupCategory IN (:categories)) " +
            "GROUP BY d")
    Page<DocumentEntity> searchByQueryAndCategories(
            @Param("query") String query,
            @Param("categories") Set<DocumentCategory> categories,
            Pageable pageable
    );
}
