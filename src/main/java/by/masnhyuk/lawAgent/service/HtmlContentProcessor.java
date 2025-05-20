package by.masnhyuk.lawAgent.service;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public interface HtmlContentProcessor {
    String cleanDocumentHtml(String html);

    Element findContentContainer(Document doc);

    void logDocumentParsingError(Document doc);
}
