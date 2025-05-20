package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.ThematicCategory;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.exception.DocumentProcessingException;
import by.masnhyuk.lawAgent.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ThematicParserServiceImpl extends BaseParserService implements ThematicParserService {
    private final LinkParser linkParser;
    private final DocumentProcessor documentProcessor;
    private final SeleniumPageFetcher seleniumPageFetcher;
    private final HtmlContentProcessor htmlContentProcessor;
    private static final Logger log = LogManager.getLogger();

    @Transactional
    @Override
    public void parseAllThematicDocuments() {
        try {
            String url = props.getBaseUrl() + props.getBusinessUrl();
            Document doc = retryableFetchPage(url);

            List<ThematicCategory> categories = linkParser.parseMainCategories(doc);
            log.info("Found {} categories to process", categories.size());

            categories.parallelStream().forEach(this::processCategory);
        } catch (Exception e) {
            log.error("Failed to parse thematic banks", e);
            throw new DocumentProcessingException("Thematic parsing failed", e);
        }
    }

    @Override
    public void processCategory(ThematicCategory category) {
        try {
            log.info("Processing category: {}", category.type.getTitle());
            parseCategoryPages(category);
        } catch (Exception e) {
            log.error("Failed to parse category: {}", category.type.getTitle(), e);
        }
    }

    @Override
    public void parseCategoryPages(ThematicCategory category) {
        Document categoryPage = retryableFetchPage(category.url);
        Elements subCategoryElements = categoryPage.select(props.getSelectors().getThematicSubCategoryItems());

        if (!subCategoryElements.isEmpty()) {
            processSubCategories(subCategoryElements, category.type);
        } else {
            processDirectDocuments(categoryPage, category.url, category.type);
        }
    }

    @Override
    public void processSubCategories(Elements subCategoryElements, DocumentCategory categoryType) {
        log.info("Found {} subcategories", subCategoryElements.size());
        subCategoryElements.stream()
                .map(element -> props.getBaseUrl() + element.attr("href"))
                .forEach(url -> parseDocumentsPage(url, categoryType));
    }

    @Override
    public void processDirectDocuments(Document categoryPage, String categoryUrl, DocumentCategory categoryType) {
        Elements directDocumentLinks = categoryPage.select(props.getSelectors().getThematicDirectDocumentLinks());
        if (!directDocumentLinks.isEmpty()) {
            log.info("Found {} direct documents", directDocumentLinks.size());
            parseDocumentsPage(categoryUrl, categoryType);
        } else {
            log.warn("No subcategories or direct documents found at: {}", categoryUrl);
        }
    }

    @Override
    public void parseDocumentsPage(String pageUrl, DocumentCategory category) {
        try {
            String renderedHtml = seleniumPageFetcher.fetchRenderedContent(pageUrl);
            Document page = Jsoup.parse(renderedHtml);

            Elements documentLinks = findDocumentLinks(page);
            log.info("Found {} document links at {}", documentLinks.size(), pageUrl);

            if (documentLinks.isEmpty()) {
                htmlContentProcessor.logDocumentParsingError(page);
                return;
            }

            documentLinks.forEach(link -> processDocumentLink(link, category));
        } catch (Exception e) {
            log.error("Failed to parse rendered page: {}", pageUrl, e);
        }
    }

    @Override
    public Elements findDocumentLinks(Document page) {
        Elements links = page.select(props.getSelectors().getThematicDocumentLinks());
        return links.isEmpty()
                ? page.select(props.getSelectors().getThematicAlternativeDocumentLinks())
                : links;
    }

    @Override
    public void processDocumentLink(Element link, DocumentCategory category) {
        try {
            LinkParserImpl.DocumentLinkInfo linkInfo = linkParser.extractDocumentLinkInfo(link);
            String content = retryableFetchHtmlContent(props.getBaseUrl() + link.attr("href"));

            documentProcessor.processDocument(
                    linkInfo.docNumber(),
                    linkInfo.title(),
                    props.getBaseUrl() + link.attr("href"),
                    linkInfo.description(),
                    category,
                    content
            );
        } catch (Exception e) {
            log.error("Failed to process document link at {}: {}", link.attr("href"), e.getMessage());
        }
    }

    @Override
    public String retryableFetchHtmlContent(String url) {
        return retryOperation(() -> {
            Document doc = retryableFetchPage(url);
            Element contentContainer = htmlContentProcessor.findContentContainer(doc);
            return htmlContentProcessor.cleanDocumentHtml(contentContainer.html());
        }, "Failed to fetch document content");
    }
}