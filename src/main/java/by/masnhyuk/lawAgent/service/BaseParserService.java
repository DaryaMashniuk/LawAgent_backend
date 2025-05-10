package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.config.PravoParserProperties;
import by.masnhyuk.lawAgent.exception.DocumentProcessingException;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;


public abstract class BaseParserService {
    protected final PravoParserProperties props;
    protected static final int MAX_RETRIES = 3;
    protected static final Duration RETRY_DELAY = Duration.ofSeconds(2);

    public BaseParserService() {
        this.props = new PravoParserProperties();
    }

    protected <T> T retryOperation(Supplier<T> operation, String errorMessage) {
        int attempts = 0;
        Exception lastException = null;

        while (attempts < MAX_RETRIES) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                attempts++;
                if (attempts < MAX_RETRIES) {
                    try {
                        Thread.sleep(RETRY_DELAY.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new DocumentProcessingException("Operation interrupted", ie);
                    }
                }
            }
        }
        throw new DocumentProcessingException(errorMessage, lastException);
    }

    protected Document retryableFetchPage(String url) {
        return retryOperation(() -> {
                    try {
                        return Jsoup.connect(url)
                                        .timeout(props.getTimeoutMs())
                                        .get();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                "Failed to fetch page: " + url);
    }
}