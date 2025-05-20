package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.ThematicCategory;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.transaction.annotation.Transactional;

public interface ThematicParserService {
    @Transactional
    void parseAllThematicDocuments();

    void processCategory(ThematicCategory category);

    void parseCategoryPages(ThematicCategory category);

    void processSubCategories(Elements subCategoryElements, DocumentCategory categoryType);

    void processDirectDocuments(Document categoryPage, String categoryUrl, DocumentCategory categoryType);

    void parseDocumentsPage(String pageUrl, DocumentCategory category);

    Elements findDocumentLinks(Document page);

    void processDocumentLink(Element link, DocumentCategory category);

    String retryableFetchHtmlContent(String url);
}
