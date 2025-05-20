package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.service.impl.DocumentProcessorImpl;
import org.springframework.transaction.annotation.Transactional;

public interface DocumentProcessor {
    @Transactional
    DocumentProcessorImpl.DocumentProcessingResult processDocument(Integer docNumber, String title, String docUrl,
                                                                   String description, DocumentCategory groupCategory,
                                                                   String content);

    DocumentProcessorImpl.DocumentProcessingResult processExistingDocument(DocumentEntity doc, String content,
                                                                           String hash, String description,
                                                                           Integer docNumber, String docUrl);

    DocumentProcessorImpl.DocumentProcessingResult createNewDocument(Integer docNumber, String title, String docUrl,
                                                                     String description, DocumentCategory groupCategory,
                                                                     String content, String hash);

    void notifySubscribers(DocumentEntity doc, DocumentVersion version);

    String extractBaseDescription(String fullDescription);
}
