package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.creator.DocumentCreator;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentRepository;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.repository.SubscriptionService;
import by.masnhyuk.lawAgent.service.DocumentProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentProcessorImpl implements DocumentProcessor {
    private static final Logger log = LogManager.getLogger();
    private final DocumentRepository documentRepo;
    private final DocumentVersionRepository versionRepo;
    private final DocumentCreator documentCreator;
    private final SubscriptionService subscriptionService;

    @Transactional
    @Override
    public DocumentProcessingResult processDocument(Integer docNumber, String title, String docUrl,
                                                    String description, DocumentCategory groupCategory,
                                                    String content) {
        String hash = DigestUtils.sha256Hex(content);
        String baseDescription = extractBaseDescription(description);
        Optional<DocumentEntity> existingDoc = documentRepo.findByBaseTitleAndDetails(title, baseDescription);

        if (existingDoc.isPresent()) {
            return processExistingDocument(existingDoc.get(), content, hash, description, docNumber, docUrl);
        } else {
            return createNewDocument(docNumber, title, docUrl, description, groupCategory, content, hash);
        }
    }

    @Override
    public DocumentProcessingResult processExistingDocument(DocumentEntity doc, String content,
                                                            String hash, String description,
                                                            Integer docNumber, String docUrl) {
        if (!versionRepo.existsByDocumentAndContentHash(doc, hash)) {
            DocumentVersion savedVersion = DocumentCreator.createDocumentVersion(
                    doc, content, hash, description, docNumber, docUrl
            );
            versionRepo.save(savedVersion);
            log.info("Saved new version for existing document: {}", doc.getId());
            notifySubscribers(doc, savedVersion);
            return new DocumentProcessingResult(doc, savedVersion);
        }
        return new DocumentProcessingResult(doc, null);
    }

    @Override
    public DocumentProcessingResult createNewDocument(Integer docNumber, String title, String docUrl,
                                                      String description, DocumentCategory groupCategory,
                                                      String content, String hash) {
        DocumentEntity doc = documentCreator.createDocument(docNumber, title, docUrl, description, groupCategory);
        documentRepo.save(doc);
        DocumentVersion savedVersion = DocumentCreator.createDocumentVersion(doc, content, hash, description, docNumber, docUrl);
        versionRepo.save(savedVersion);
        log.info("Created new document: {}", doc);
        notifySubscribers(doc, savedVersion);
        return new DocumentProcessingResult(doc, savedVersion);
    }

    @Override
    public void notifySubscribers(DocumentEntity doc, DocumentVersion version) {
        try {
            subscriptionService.notifySubscribers(version);
        } catch (Exception e) {
            log.error("Failed to send notifications for document {}", doc.getId(), e);
        }
    }

    @Override
    public String extractBaseDescription(String fullDescription) {
        String[] splitByFrom = fullDescription.split(" от ");
        String[] splitByNumber = fullDescription.split(" №");

        String base = splitByFrom[0];
        if (splitByNumber.length > 1 && splitByNumber[0].length() < base.length()) {
            base = splitByNumber[0];
        }

        return base.trim();
    }

    public record DocumentProcessingResult(DocumentEntity doc, DocumentVersion savedVersion) {}
}
