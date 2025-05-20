package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.config.PravoParserProperties;
import by.masnhyuk.lawAgent.exception.DocumentProcessingException;
import by.masnhyuk.lawAgent.service.HtmlContentProcessor;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class HtmlContentProcessorImpl implements HtmlContentProcessor {
    private static final Logger log = LogManager.getLogger();
    private final PravoParserProperties props;

    @Override
    public String cleanDocumentHtml(String html) {
        html = html.replaceAll("(background:[^;'\"]*;?)", "")
                .replaceAll(";%", "")
                .replaceAll("[a-zA-Z]+:#[a-zA-Z]+", "");

        html = html.replaceAll("<([a-z]+)[^>]*>\\s*</\\1>", "")
                .replaceAll("\\s+", " ")
                .replaceAll("style=\"\"", "")
                .trim();

        return html;
    }

    @Override
    public Element findContentContainer(Document doc) {
        Element contentContainer = doc.selectFirst(props.getSelectors().getThematicContentContainers());

        if (contentContainer == null) {
            log.error("Document content not found. Trying alternative selectors...");
            contentContainer = doc.selectFirst(props.getSelectors().getThematicAlternativeContentContainers());
        }

        if (contentContainer == null) {
            logDocumentParsingError(doc);
            throw new DocumentProcessingException("Document content container not found");
        }

        contentContainer.select(props.getSelectors().getThematicElementsToRemove()).remove();
        return contentContainer;
    }

    @Override
    public void logDocumentParsingError(Document doc) {
        String debugPath = "debug.html";
        try {
            Files.write(Paths.get(debugPath), doc.html().getBytes());
            log.error("Document content not found. Full page saved to {}", debugPath);
        } catch (IOException e) {
            log.error("Failed to save debug file", e);
        }
    }
}