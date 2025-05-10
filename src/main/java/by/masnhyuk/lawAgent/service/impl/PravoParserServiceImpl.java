package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.PdfParseResult;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.exception.DocumentProcessingException;
import by.masnhyuk.lawAgent.exception.PdfProcessingException;
import by.masnhyuk.lawAgent.repository.DocumentRepository;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import by.masnhyuk.lawAgent.creator.DocumentCreator;
import by.masnhyuk.lawAgent.service.BaseParserService;

@Service
@RequiredArgsConstructor
public class PravoParserServiceImpl extends BaseParserService {
    private final DocumentRepository documentRepo;
    private final DocumentVersionRepository versionRepo;
    private final PdfParserService pdfParserService;
    private final ThematicParserService thematicParserService;
    private final DocumentCreator documentCreator;
    private static final Logger log = LogManager.getLogger();

    @Scheduled(cron = "${parser.pravo.cron:0 0 9 * * ?}")
    @Transactional
    public void autoParseNewDocuments() {
        log.info("Starting automatic documents parsing");
        parseNewDocuments();
        thematicParserService.parseAllThematicDocuments();
    }

    @Transactional
    public void manualParseNewDocuments() {
        log.info("Starting manual documents parsing");
        //parseNewDocuments();
        thematicParserService.parseAllThematicDocuments();
    }

    @Transactional
    public void parseNewDocuments() {
        try {
            String mainUrl = props.getBaseUrl() + props.getNewDocumentsUrl();
            Document mainPage = retryableFetchPage(mainUrl);
            parseDocumentsPage(mainPage);
        } catch (Exception e) {
            log.error("Pravo.by parsing failed", e);
            throw new DocumentProcessingException("Failed to parse documents", e);
        }
    }

    private void parseDocumentsPage(Document page) {
        page.select(props.getSelectors().getDocumentSection()).parallelStream()
                .forEach(this::processDocumentSection);
    }

    private void processDocumentSection(Element section) {
        try {
            String number = extractDocumentNumber(section);
            Element link = section.selectFirst(props.getSelectors().getDocumentLink());
            if (link == null) return;

            String title = link.text();
            String docUrl = props.getBaseUrl() + link.attr("href");
            String details = section.select(props.getSelectors().getDocumentDetails()).text();

            processDocument(number, title, docUrl, details);
        } catch (Exception e) {
            log.warn("Failed to parse document section", e);
        }
    }

    private void processDocument(String number, String title, String url, String details) {
        DocumentEntity doc = documentRepo.findByNumber(number)
                .orElseGet(() -> documentRepo.save(
                        documentCreator.createDocument(number, title, url, details)
                ));

        PdfParseResult parseResult = retryableFetchPdfContent(url);
        String textHash = DigestUtils.sha256Hex(parseResult.getTextContent());
        String pdfHash = DigestUtils.sha256Hex(parseResult.getPdfContent());

        if (!versionRepo.existsByDocumentAndContentHash(doc, textHash)) {
            versionRepo.save(
                    DocumentCreator.createPdfDocumentVersion(
                            doc,
                            parseResult.getTextContent(),
                            parseResult.getPdfContent(),
                            textHash,
                            pdfHash
                    )
            );
            log.info("Saved new version for document {}", number);
        }
    }

    private PdfParseResult retryableFetchPdfContent(String url) {
        return retryOperation(() -> {
            try {
                return pdfParserService.parsePdfContent(url);
            } catch (Exception e) {
                throw new PdfProcessingException("Failed to parse pdf", e);
            }
        }, "Failed to fetch PDF content");
    }

    private String extractDocumentNumber(Element section) {
        String rawNumber = section.select(props.getSelectors().getDocumentNumber()).text();
        return rawNumber.split("<br>")[0].trim();
    }

}