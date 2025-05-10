package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.ThematicCategory;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.exception.DocumentProcessingException;
import by.masnhyuk.lawAgent.repository.DocumentRepository;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.service.BaseParserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import by.masnhyuk.lawAgent.creator.DocumentCreator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThematicParserService extends BaseParserService {
    private final DocumentRepository documentRepo;
    private final DocumentVersionRepository versionRepo;
    private final DocumentCreator documentCreator;
    private static final Logger log = LogManager.getLogger();
    private static final Pattern DOCUMENT_NUMBER_PATTERN = Pattern.compile("№\\s*(\\d+-?\\w*)");

    @Transactional
    public void parseAllThematicDocuments() {
        try {
            List<ThematicCategory> categories = parseMainCategories();
            log.info(categories.toString());
            categories.parallelStream().forEach(category -> {
                try {
                    log.info("Parsing " ,category.toString());
                    parseCategoryPages(category);
                } catch (Exception e) {
                    log.error("Failed to parse category: {}", category.type.getTitle(), e);
                }
            });
        } catch (Exception e) {
            log.error("Failed to parse thematic banks", e);
            throw new DocumentProcessingException("Thematic parsing failed", e);
        }
    }

    private List<ThematicCategory> parseMainCategories() {
        String url = props.getBaseUrl() + props.getBusinessUrl();
        Document doc = retryableFetchPage(url);
        List<ThematicCategory> categories = new ArrayList<>();

        Elements categoryElements = doc.select(props.getSelectors().getThematicMainPageItems());

        for (Element categoryElement : categoryElements) {
            try {
                // Ищем заголовок внутри элемента категории
                Element titleElement = categoryElement.selectFirst(props.getSelectors().getThematicCategoryTitle());
                if (titleElement == null) {
                    log.warn("No title found for category element: {}", categoryElement);
                    continue;
                }

                String title = titleElement.text().trim();
                String href = categoryElement.attr("href");

                try {
                    DocumentCategory category = DocumentCategory.fromTitle(title);
                    categories.add(new ThematicCategory(
                            props.getBaseUrl() + href,
                            category
                    ));
                    log.info("Added category: {} with URL: {}", title, href);
                } catch (Exception e) {
                    log.warn("Unknown category title: {}", title);
                }
            } catch (Exception e) {
                log.error("Error processing category element", e);
            }
        }

        return categories;
    }

    private void parseCategoryPages(ThematicCategory category) {
        try {
            Document categoryPage = retryableFetchPage(category.url);

            // Парсим подкатегории (если есть)
            Elements subCategoryElements = categoryPage.select(props.getSelectors().getThematicSubPageItems());
            if (!subCategoryElements.isEmpty()) {
                log.info("Found {} subcategories", subCategoryElements.size());
                for (Element subCategoryElement : subCategoryElements) {
                    String subCategoryUrl = props.getBaseUrl() + subCategoryElement.attr("href");
                    parseDocumentsPage(subCategoryUrl, category.type);
                }
            } else {
                // Если нет подкатегорий, парсим текущую страницу
                log.info("No subcategories found, checking for direct documents");
                Elements directDocumentLinks = categoryPage.select("div.content a.link_to");
                if (!directDocumentLinks.isEmpty()) {
                    log.info("Found {} direct documents", directDocumentLinks.size());
                    parseDocumentsPage(category.url, category.type);
                } else {
                    log.warn("No subcategories or direct documents found at: {}", category.url);
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse category: {}", category.type, e);
        }
    }

    private void parseDocumentsPage(String pageUrl, DocumentCategory category) {
        try {
            String renderedHtml = fetchRenderedContent(pageUrl);
            Document page = Jsoup.parse(renderedHtml);

            // Селекторы для динамически загруженного контента
            Elements documentLinks = page.select("a.link_to[href*=/document/]");

            if (documentLinks.isEmpty()) {
                // Альтернативные селекторы
                documentLinks = page.select("div.content a[href*='guid']");
            }

            log.info("Found {} document links at {}", documentLinks.size(), pageUrl);

            if (documentLinks.isEmpty()) {
                log.warn("No documents found after JS rendering. Page source saved to debug.html");
                Files.write(Paths.get("debug.html"), renderedHtml.getBytes());
                return;
            }

            documentLinks.forEach(link -> processDocumentLink(link, category));
        } catch (Exception e) {
            log.error("Failed to parse rendered page: {}", pageUrl, e);
        }
    }

    private void processDocumentLink(Element link, DocumentCategory category) {
        try {
            String docUrl = props.getBaseUrl() + link.attr("href");

            // Улучшенное извлечение заголовка
            String title = link.select("div.item_title, .title, h3, h4").stream()
                    .map(Element::text)
                    .filter(t -> !t.isBlank())
                    .findFirst()
                    .orElseGet(() -> link.text().trim().split("\n")[0]);

            // Улучшенное извлечение описания
            String description = link.select("div.item_description, .description").stream()
                    .map(Element::text)
                    .filter(d -> !d.isBlank())
                    .findFirst()
                    .orElse("");

            processThematicDocument(
                    extractDocumentNumber(description).or(() -> extractDocumentNumber(title)).orElse(null),
                    title.trim(),
                    docUrl,
                    description.trim(),
                    category
            );
        } catch (Exception e) {
            log.error("Failed to process document link at {}: {}", link.attr("href"), e.getMessage());
        }
    }

    private Optional<String> extractDocumentNumber(String description) {
        Matcher matcher = DOCUMENT_NUMBER_PATTERN.matcher(description);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    @Transactional
    private void processThematicDocument(String number, String title, String docUrl,
                                         String description, DocumentCategory category) {
        try {
            String content = retryableFetchHtmlContent(docUrl);
            String hash = DigestUtils.sha256Hex(content);

            log.info("Processing document: {}", title);

            // Поиск или создание документа
            DocumentEntity doc = documentRepo.findByNumber(number)
                    .orElseGet(() -> {
                        DocumentEntity newDoc = documentCreator.createDocument(
                                number, title, docUrl, description
                        );
                        log.info("Creating new document: {}", newDoc);
                        return documentRepo.save(newDoc);
                    });

            // Проверка и сохранение версии
            if (!versionRepo.existsByDocumentAndContentHash(doc, hash)) {
                versionRepo.save(DocumentCreator.createDocumentVersion(doc, content, hash));
                log.info("Saved new version for document: {}", doc.getId());
            } else {
                log.info("Document already exists with same content hash: {}", hash);
            }
        } catch (Exception e) {
            log.error("Failed to process document {}: {}", title, e.getMessage());
            throw new DocumentProcessingException("Failed to save document", e);
        }
    }

    private String fetchRenderedContent(String url) {
        System.setProperty("webdriver.chrome.driver", "C:/Program Files/chromedriver-win64/chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new", // Новый headless режим
                "--disable-gpu",
                "--no-sandbox",
                "--remote-allow-origins=*",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080"
        );

        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(url);

            // Явное ожидание загрузки контента
            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> ((JavascriptExecutor)d)
                            .executeScript("return document.readyState").equals("complete"));

            // Дополнительное ожидание для динамического контента
            Thread.sleep(1000);

            return driver.getPageSource();
        } catch (Exception e) {
            log.error("Error fetching rendered content", e);
            throw new DocumentProcessingException("Failed to render page", e);
        } finally {
            driver.quit();
        }
    }

    private String retryableFetchHtmlContent(String url) {
        return retryOperation(() -> {
            Document doc = retryableFetchPage(url);

            // Ищем основной контейнер с текстом документа
            Element contentContainer = doc.selectFirst("div.Section1");
            if (contentContainer == null) {
                log.error("Document content not found. Trying alternative selectors...");
                // Альтернативные селекторы
                contentContainer = doc.selectFirst("div.document-content, div.content, div.text");
            }

            if (contentContainer == null) {
                String debugPath = "debug.html";
                try {
                    Files.write(Paths.get(debugPath), doc.html().getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                log.error("Document content not found. Full page saved to {}", debugPath);
                throw new DocumentProcessingException("Document content container not found");
            }

            // Очистка контента от ненужных элементов
            contentContainer.select("script, style, noscript, iframe, div.ya-share2, div.backnav, div.linkcart").remove();

            // Получаем чистый текст документа
            String cleanText = contentContainer.text()
                    .replaceAll("\\s+", " ") // Заменяем множественные пробелы на один
                    .trim();

            log.debug("Extracted document text: {}", cleanText.substring(0, Math.min(100, cleanText.length())) + "...");

            return cleanText;
        }, "Failed to fetch document content");
    }

}