package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.config.PravoParserProperties;
import by.masnhyuk.lawAgent.dto.PdfParseResult;
import by.masnhyuk.lawAgent.exception.PdfNotFoundException;
import by.masnhyuk.lawAgent.exception.PdfTooLargeException;
import by.masnhyuk.lawAgent.util.HeadingAwareStripper;
import io.github.jonathanlink.PDFLayoutTextStripper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;

import technology.tabula.Table;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class PdfParserService {
    private final PravoParserProperties props;
    private static final int MAX_PDF_SIZE_MB = 10;
    private static final Pattern HEADER_FOOTER_PATTERN =
            Pattern.compile("Национальный правовой Интернет-портал Республики Беларусь.*?(\\n|$)|\\d+\\s*/\\s*\\d+");
    private static final String WHITESPACE_IN_THE_BEGINNING= "(?m)^\\s+";
    private static final String WHITESPACE_IN_THE_END= "(?m)\\s+$";

    public PdfParseResult parsePdfContent(String documentUrl) throws IOException {
        Document docPage = Jsoup.connect(documentUrl)
                .timeout(props.getTimeoutMs())
                .get();

        String pdfUrl = extractPdfUrl(docPage);
        if (pdfUrl == null) {
            throw new PdfNotFoundException("PDF URL not found on page");
        }

        return parsePdfFromUrl(pdfUrl);
    }

    private PdfParseResult parsePdfFromUrl(String pdfUrl) throws IOException {
        try (InputStream pdfStream = new URL(pdfUrl).openStream();
             BufferedInputStream bufferedStream = new BufferedInputStream(pdfStream);
             PDDocument document = PDDocument.load(bufferedStream)) {


            ByteArrayOutputStream pdfBytes = new ByteArrayOutputStream();
            document.save(pdfBytes);
            checkPdfSize(document, pdfBytes.toByteArray() );
            String text= extractAndCleanText(document);

            return new PdfParseResult(text, pdfBytes.toByteArray());
        }
    }

    private void checkPdfSize(PDDocument document, byte[] pdfBytes) {
        if (pdfBytes.length > MAX_PDF_SIZE_MB * 1024 * 1024) {
            throw new PdfTooLargeException(
                    String.format("PDF size exceeds maximum allowed %dMB", MAX_PDF_SIZE_MB));
        }
    }




    private String extractAndCleanText(PDDocument document) throws IOException {
        return extractHtmlWithTablesAndHeadings(document);
    }

    private String extractHtmlWithTablesAndHeadings(PDDocument document) throws IOException {
        ObjectExtractor extractor = new ObjectExtractor(document);
        SpreadsheetExtractionAlgorithm tableExtractor = new SpreadsheetExtractionAlgorithm();
        PDFTextStripper stripper = new PDFLayoutTextStripper();

        StringBuilder html = new StringBuilder();

        for (int pageIndex = 1; pageIndex <= document.getNumberOfPages(); pageIndex++) {
            Page page = extractor.extract(pageIndex);
            List<Table> tables = tableExtractor.extract(page);

            html.append("<div class='l-main-content page'>\n");

            // Добавляем таблицы
            for (Table table : tables) {
                html.append("<table>\n");
                for (List<RectangularTextContainer> row : table.getRows()) {
                    html.append("<tr>");
                    for (RectangularTextContainer cell : row) {
                        html.append("<td>").append(escapeHtml(cell.getText())).append("</td>");
                    }
                    html.append("</tr>\n");
                }
                html.append("</table>\n");
            }

            // Добавляем текст с распознаванием заголовков
            PDPage pdPage = document.getPage(pageIndex - 1);
            HeadingAwareStripper headingStripper = new HeadingAwareStripper();
            headingStripper.setStartPage(pageIndex);
            headingStripper.setEndPage(pageIndex);

            String pageHtml = headingStripper.getText(document);
            html.append(pageHtml);

            html.append("</div>\n");
        }


        return html.toString();
    }


    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br/>");
    }


    private String cleanText(String text) {


        return HEADER_FOOTER_PATTERN.matcher(text)
                .replaceAll("")
                .replaceAll("\\d{1,2}\\.\\d{1,2}\\.\\d{4}, \\d+/\\d+", "")
                .replaceAll("Стр\\. \\d+ из \\d+", "")
                .replaceAll(WHITESPACE_IN_THE_BEGINNING, "")
                .replaceAll(WHITESPACE_IN_THE_END, "")
                .replaceAll("(?<=\\S)  +(?=\\S)", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("(?<=\\S)  +(?=\\S)", " ")
                .trim();
    }

    private String extractPdfUrl(Document docPage) {
        Element pdfObject = docPage.selectFirst("object[type=application/pdf]");
        if (pdfObject != null) {
            return props.getBaseUrl() + pdfObject.attr("data");
        }

        Element pdfLink = docPage.selectFirst("a[href$=.pdf]");
        if (pdfLink != null) {
            return props.getBaseUrl() + pdfLink.attr("href");
        }

        return null;
    }

}