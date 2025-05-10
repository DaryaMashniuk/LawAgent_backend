package by.masnhyuk.lawAgent.util;

import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class HeadingAwareStripper extends PDFTextStripper {
    public HeadingAwareStripper() throws IOException {
        super();
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        if (textPositions == null || textPositions.isEmpty()) return;

        float avgFontSize = (float) textPositions.stream()
                .mapToDouble(TextPosition::getFontSizeInPt)
                .average()
                .orElse(10.0);

        String tag;
        if (avgFontSize >= 16) {
            tag = "h1";
        } else if (avgFontSize >= 14) {
            tag = "h2";
        } else if (avgFontSize >= 12) {
            tag = "h3";
        } else {
            tag = "p";
        }

        getOutput().append("<").append(tag).append(">")
                .append(escapeHtml(string.trim()))
                .append("</").append(tag).append(">\n");
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br/>");
    }
}
