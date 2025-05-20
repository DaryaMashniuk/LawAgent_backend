package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.config.PravoParserProperties;
import by.masnhyuk.lawAgent.dto.ThematicCategory;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.service.LinkParser;
import by.masnhyuk.lawAgent.util.DocumentNumberExtractor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LinkParserImpl implements LinkParser {
    private static final Logger log = LogManager.getLogger();
    private final PravoParserProperties props;

    @Override
    public List<ThematicCategory> parseMainCategories(Document doc) {
        return doc.select(props.getSelectors().getThematicMainPageItems()).stream()
                .map(this::extractCategoryFromElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ThematicCategory> extractCategoryFromElement(Element categoryElement) {
        try {
            Element titleElement = categoryElement.selectFirst(props.getSelectors().getThematicCategoryTitle());
            if (titleElement == null) {
                log.warn("No title found for category element: {}", categoryElement);
                return Optional.empty();
            }

            String title = titleElement.text().trim();
            String href = categoryElement.attr("href");

            try {
                DocumentCategory category = DocumentCategory.fromTitle(title);
                ThematicCategory result = new ThematicCategory(
                        props.getBaseUrl() + href,
                        category
                );
                log.debug("Added category: {} with URL: {}", title, href);
                return Optional.of(result);
            } catch (Exception e) {
                log.warn("Unknown category title: {}", title);
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Error processing category element", e);
            return Optional.empty();
        }
    }

    @Override
    public DocumentLinkInfo extractDocumentLinkInfo(Element link) {
        String title = link.select(props.getSelectors().getThematicDocumentTitle()).stream()
                .map(Element::text)
                .filter(t -> !t.isBlank())
                .findFirst()
                .orElseGet(() -> link.text().trim().split("\n")[0]);

        String description = link.select(props.getSelectors().getThematicDocumentDescription()).stream()
                .map(Element::text)
                .filter(d -> !d.isBlank())
                .findFirst()
                .orElse("");

        Integer docNumber = DocumentNumberExtractor.extractNumber(description);
        if (docNumber == null) {
            docNumber = DocumentNumberExtractor.extractNumber(title);
        }

        return new DocumentLinkInfo(
                docNumber,
                title.trim(),
                description.trim()
        );
    }

    public record DocumentLinkInfo(Integer docNumber, String title, String description) {}
}
