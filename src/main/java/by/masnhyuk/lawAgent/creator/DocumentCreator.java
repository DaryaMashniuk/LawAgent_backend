package by.masnhyuk.lawAgent.creator;

import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.SubscriptionService;
import by.masnhyuk.lawAgent.service.impl.CategoryDetectionService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Set;
@Component
@RequiredArgsConstructor
public class DocumentCreator {
    private final CategoryDetectionService categoryDetectionService;

    public DocumentEntity createDocument(Integer number, String title, String url, String details, DocumentCategory groupCategory) {
        DocumentEntity doc = new DocumentEntity();
        doc.setNumber(number);
        doc.setTitle(title);
        doc.setSourceUrl(url);
        doc.setDetails(details);
        doc.setGroupCategory(groupCategory);

        Set<DocumentCategory> categories = categoryDetectionService.detectCategories(title + " " + details);
        doc.setCategories(categories);

        return doc;
    }

    public static DocumentVersion createDocumentVersion(DocumentEntity doc, String content,
                                                        String contentHash,String description,Integer docNumber,String docUrl) {
        DocumentVersion version = new DocumentVersion();
        version.setDocument(doc);
        version.setNumber(docNumber);
        version.setDetails(description);
        version.setSourceUrl(docUrl);
        version.setContent(content);
        version.setContentHash(contentHash);
        version.setCreatedAt(LocalDateTime.now());

        return version;
    }

    public static DocumentVersion createPdfDocumentVersion(DocumentEntity doc, String textContent,
                                                           byte[] pdfContent, String textHash,
                                                           String pdfHash,Integer docNumber,String docUrl,String description) {
        DocumentVersion version = new DocumentVersion();
        version.setDocument(doc);
        version.setContent(textContent);
        version.setPdfContent(pdfContent);
        version.setContentHash(textHash);
        version.setPdfContentHash(pdfHash);
        version.setCreatedAt(LocalDateTime.now());
        version.setNumber(docNumber);
        version.setDetails(description);
        version.setSourceUrl(docUrl);
        return version;
    }
}