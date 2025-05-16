package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.dto.ComparisonResult;
import by.masnhyuk.lawAgent.dto.DocumentWithVersionsDto;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.service.impl.DocumentComparisonService;
import by.masnhyuk.lawAgent.service.impl.DocumentsService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("lawAgent/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentVersionRepository versionRepo;
    private final DocumentsService documentsService;
    private final DocumentComparisonService documentComparisonService;
    private static final Logger logger = LogManager.getLogger();
    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentById(@PathVariable("id") UUID documentId) {
        return documentsService.getDocumentById(documentId)
                .map(document -> ResponseEntity.ok(Map.of(
                        "message", "Document with id ",
                        "id", document.getId(),
                        "document",document
                )))
                .orElseGet(() -> ResponseEntity.badRequest()
                        .body(Map.of("error", "No such document")));
    }

    @GetMapping("/{documentId}/versions")
    public ResponseEntity<List<DocumentVersion>> getDocumentVersions(@PathVariable UUID documentId) {
        List<DocumentVersion> versions = documentComparisonService.getDocumentVersions(documentId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/versions/{versionId}")
    public ResponseEntity<?> getVersionById(@PathVariable UUID versionId) {
        return documentsService.getDocumentVersionById(versionId)
                .map(version -> ResponseEntity.ok(Map.of(
                        "message", "Document version found",
                        "version", version
                )))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Document version not found")));
    }

    @GetMapping("/compare")
    public ResponseEntity<ComparisonResult> compareVersions(
            @RequestParam UUID firstVersionId,
            @RequestParam UUID secondVersionId) {

        ComparisonResult result = documentComparisonService.compareVersions(firstVersionId, secondVersionId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{documentId}/latest-changes")
    public ResponseEntity<ComparisonResult> getLatestChanges(@PathVariable UUID documentId) {
        List<DocumentVersion> versions = documentComparisonService.getDocumentVersions(documentId);

        if (versions.size() < 2) {
            return ResponseEntity.ok(new ComparisonResult(
                    List.of(),
                    false,
                    null,
                    versions.isEmpty() ? null : versions.get(0).getCreatedAt(),
                    null,
                    versions.isEmpty() ? null : versions.get(0).getId(),
                    versions.isEmpty() ? null : versions.get(0).getContent(),
                    versions.isEmpty() ? null : versions.get(1).getContent()
            ));
        }

        ComparisonResult result = documentComparisonService.compareVersions(
                versions.get(1).getId(),
                versions.get(0).getId()
        );

        return ResponseEntity.ok(result);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<DocumentWithVersionsDto>> searchDocuments(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Set<DocumentCategory> categories,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("title").ascending());

        Page<DocumentWithVersionsDto> result = documentsService.searchDocuments(query, categories, pageable);

        return ResponseEntity.ok(result);
    }

}