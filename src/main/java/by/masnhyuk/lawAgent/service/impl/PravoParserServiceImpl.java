package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.config.PravoParserProperties;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentRepository;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PravoParserServiceImpl {
    private final PravoParserProperties props;
    private final DocumentRepository documentRepo;
    private final DocumentVersionRepository versionRepo;

    @Scheduled(cron = "${parser.pravo.cron:0 0 9 * * ?}")
    public void parseNewDocuments() {
        try {
            String mainUrl = props.getBaseUrl() + props.getNewDocumentsUrl();
            Document mainPage = Jsoup.connect(mainUrl)
                    .timeout(props.getTimeoutMs())
                    .get();

            // Парсим текущие документы (последний день)
            parseDocumentsPage(mainUrl);

//            // Парсим архив по датам
//            parseArchiveDates(mainPage);

        } catch (Exception e) {
            log.error("Pravo.by parsing failed", e);
        }
    }

//    private void parseArchiveDates(Document mainPage) {
//        Elements dateLinks = (Elements) mainPage.select(props.getSelectors().getDateFilter());
//        dateLinks.stream()
//                .limit(props.getMaxPages())
//                .forEach(link -> {
//                    try {
//                        String url = props.getBaseUrl() + link.attr("href");
//                        parseDocumentsPage(url);
//                        Thread.sleep(props.getTimeoutMs() / 3);
//                    } catch (Exception e) {
//                        log.warn("Failed to parse archive page", e);
//                    }
//                });
//    }

    private void parseDocumentsPage(String pageUrl) throws IOException {
        Document doc = Jsoup.connect(pageUrl)
                .timeout(props.getTimeoutMs())
                .get();

        // Обрабатываем каждый раздел документов
        doc.select(props.getSelectors().getDocumentSection()).forEach(section -> {
            try {
                String number = section.select(props.getSelectors().getDocumentNumber()).text()
                        .split("<br>")[0].trim();

                Element link = section.select(props.getSelectors().getDocumentLink()).first();
                String title = link.text();
                String docUrl = props.getBaseUrl() + link.attr("href");

                String details = section.select(props.getSelectors().getDocumentDetails()).text();
                String typeAndNumber = details.replace(title, "").trim();

                processDocument(number, title, docUrl, typeAndNumber);

            } catch (Exception e) {
                log.warn("Failed to parse document section", e);
            }
        });
    }

    private void processDocument(String number, String title, String url, String details) {
        try {
            DocumentEntity doc = documentRepo.findByNumber(number)
                    .orElseGet(() -> {
                        DocumentEntity newDoc = new DocumentEntity();
                        newDoc.setNumber(number);
                        newDoc.setTitle(title);
                        newDoc.setSourceUrl(url);
                        newDoc.setDetails(details);
                        newDoc.setCategory(detectCategory(title));
                        return documentRepo.save(newDoc);
                    });

            String content = fetchDocumentContent(url);
            String hash = DigestUtils.sha256Hex(content);

            if (versionRepo.findByDocumentAndHash(doc, hash).isEmpty()) {
                DocumentVersion version = new DocumentVersion();
                version.setDocument(doc);
                version.setContent(content);
                version.setContentHash(hash);
                version.setCreatedAt(LocalDateTime.now());
                versionRepo.save(version);
                log.info("Saved new version for document {}", number);
            }
        } catch (Exception e) {
            log.error("Failed to process document {}", number, e);
        }
    }

    private String fetchDocumentContent(String url) throws IOException {
        Document doc = Jsoup.connect(url)
                .timeout(props.getTimeoutMs())
                .get();
        return doc.select("div.document-content").text(); // Основное содержимое документа
    }

    private String detectCategory(String title) {
        // Логика определения категории по названию
        if (title.contains("Постановление")) return "DECREE";
        if (title.contains("Закон")) return "LAW";
        return "OTHER";
    }
}