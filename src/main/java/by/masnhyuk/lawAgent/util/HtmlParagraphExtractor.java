package by.masnhyuk.lawAgent.util;

import by.masnhyuk.lawAgent.dto.ParagraphPair;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class HtmlParagraphExtractor {

    public static List<ParagraphPair> extractParagraphs(String html) {
        if (html == null || html.isBlank()) return Collections.emptyList();

        String processedHtml = html
                .replaceAll("(?i)<div[^>]*>", "")
                .replaceAll("(?i)</div>", "")
                .replaceAll("(?i)<\\s*(p|h[1-6])[^>]*>", "\n$0")
                .replaceAll("(?i)</\\s*(p|h[1-6])>", "$0\n")
                .replaceAll("[ \\t]+", " ")
                .trim();

        String[] rawSplits = processedHtml.split("\n");

        List<ParagraphPair> result = new ArrayList<>();
        for (String raw : rawSplits) {
            String plain = raw.replaceAll("<[^>]+>", "").trim();
            if (!plain.isEmpty()) {
                result.add(new ParagraphPair(raw.trim(), plain));
            }
        }

        return result;

    }
}