package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.ThematicCategory;
import by.masnhyuk.lawAgent.service.impl.LinkParserImpl;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Optional;

public interface LinkParser {
    List<ThematicCategory> parseMainCategories(Document doc);

    Optional<ThematicCategory> extractCategoryFromElement(Element categoryElement);

    LinkParserImpl.DocumentLinkInfo extractDocumentLinkInfo(Element link);
}
