package by.masnhyuk.lawAgent.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PdfParseResult {
    private String textContent;
    private byte[] pdfContent;
}
