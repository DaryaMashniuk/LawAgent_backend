package by.masnhyuk.lawAgent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "parser.pravo")
public class PravoParserProperties {
    private String baseUrl = "https://pravo.by";
    private String newDocumentsUrl = "/ofitsialnoe-opublikovanie/novye-postupleniya/";
    private int maxPages = 5;
    private int timeoutMs = 15000;

    @Data
    public static class Selectors {
        private String dateFilter = "ul.header-nav li a";
        private String documentSection = "span.section-title.color-grey + h3 + dl";
        private String documentNumber = "dt";
        private String documentLink = "dd p a";
        private String documentTitle = "dd p a";
        private String documentDetails = "dd p";
    }

    private Selectors selectors = new Selectors();
}
