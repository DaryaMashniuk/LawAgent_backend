package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.exception.DocumentProcessingException;
import by.masnhyuk.lawAgent.service.SeleniumPageFetcher;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class SeleniumPageFetcherImpl implements SeleniumPageFetcher {
    private static final Logger log = LogManager.getLogger();

    @Override
    public String fetchRenderedContent(String url) {
        System.setProperty("webdriver.chrome.driver", "C:/Program Files/chromedriver-win64/chromedriver.exe");
        ChromeOptions options = new ChromeOptions();
        options.addArguments(
                "--headless=new",
                "--disable-gpu",
                "--no-sandbox",
                "--remote-allow-origins=*",
                "--disable-dev-shm-usage",
                "--window-size=1920,1080"
        );

        WebDriver driver = new ChromeDriver(options);
        try {
            driver.get(url);

            new WebDriverWait(driver, Duration.ofSeconds(10))
                    .until(d -> ((JavascriptExecutor)d)
                            .executeScript("return document.readyState").equals("complete"));

            Thread.sleep(1000);
            return driver.getPageSource();
        } catch (Exception e) {
            log.error("Error fetching rendered content", e);
            throw new DocumentProcessingException("Failed to render page", e);
        } finally {
            driver.quit();
        }
    }
}
