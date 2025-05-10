package by.masnhyuk.lawAgent.exception;

public class PdfNotFoundException extends PdfProcessingException {
  public PdfNotFoundException(String url) {
    super("PDF not found at URL: " + url);
  }
}
