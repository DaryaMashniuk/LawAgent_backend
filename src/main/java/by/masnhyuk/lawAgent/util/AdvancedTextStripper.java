package by.masnhyuk.lawAgent.util;

import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.List;

public class AdvancedTextStripper extends PDFTextStripper {
    private static final float HEADER_FOOTER_MARGIN_RATIO = 0.1f;
    private static final float LEFT_MARGIN_RATIO = 0.1f;

    private float currentPageHeight;
    private float currentPageWidth;

    public AdvancedTextStripper() throws IOException {
        super();
        setSuppressDuplicateOverlappingText(true);
    }

    @Override
    protected void startPage(PDPage page) throws IOException {
        super.startPage(page);
        this.currentPageHeight = page.getMediaBox().getHeight();
        this.currentPageWidth = page.getMediaBox().getWidth();
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        float headerFooterMargin = currentPageHeight * HEADER_FOOTER_MARGIN_RATIO;

        if (text.getY() < headerFooterMargin ||
                text.getY() > (currentPageHeight - headerFooterMargin)) {
            return;
        }

        if (text.getX() < currentPageWidth * LEFT_MARGIN_RATIO) {
            return;
        }

        super.processTextPosition(text);
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException {
        if (!textPositions.isEmpty()) {
            float fontSize = textPositions.get(0).getFontSizeInPt();

            if (fontSize >= 16) {
                text = "\n\n### " + text.trim() + " ###\n\n";
            } else if (fontSize >= 14) {
                text = "\n\n## " + text.trim() + " ##\n\n";
            } else if (fontSize >= 12) {
                text = "\n\n# " + text.trim() + " #\n\n";
            }
        }

        super.writeString(text, textPositions);
    }

}