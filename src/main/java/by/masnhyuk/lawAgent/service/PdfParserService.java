package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.PdfParseResult;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jsoup.nodes.Document;

import java.io.IOException;

public interface PdfParserService {
    PdfParseResult parsePdfContent(String documentUrl) throws IOException;

    PdfParseResult parsePdfFromUrl(String pdfUrl) throws IOException;

    void checkPdfSize(PDDocument document, byte[] pdfBytes);

    String extractAndCleanText(PDDocument document) throws IOException;

    String extractHtmlWithTablesAndHeadings(PDDocument document) throws IOException;

    String escapeHtml(String text);

    String cleanText(String text);

    String extractPdfUrl(Document docPage);
}
