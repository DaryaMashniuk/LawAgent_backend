package by.masnhyuk.lawAgent.exception;

public class PdfParseException extends DocumentProcessingException {
    public PdfParseException(String message) { super(message); }
    public PdfParseException(String message, Throwable cause) { super(message, cause); }
}
