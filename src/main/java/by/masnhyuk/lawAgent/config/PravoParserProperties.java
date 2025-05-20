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
    private String businessUrl = "/pravovaya-informatsiya/pravovye-akty-po-temam/obshchie-polozheniya/";
    private int maxPages = 5;
    private int timeoutMs = 15000;
    private int maxPdfSizeMb = 10;
    private int retryCount = 3;
    private int retryDelaySec = 2;
    private int parsingThreads = 4;

    @Data
    public static class Selectors {
        private String dateFilter = "ul.header-nav li a";
        private String documentSection = "dl";
        private String documentNumber = "dt";
        private String documentLink = "dd p a";
        private String documentTitle = "dd p a";
        private String documentDetails = "dd p";
        private String thematicMainPageItems = "a.block-link";
        private String thematicSubPageItems = "div.structure div.item a.link";
        private String thematicLinkToDocuments = "a.link_to";
        private String thematicDocumentContent = "div.l-main-content.l-main_bg.l-main_popup";
        private String thematicDocumentHeader = "div.l-main__header";
        private String thematicCategoryTitle = "div.title";
        private String thematicSubCategoryItems = "div.structure div.item a.link";
        private String thematicDirectDocumentLinks = "div.content a.link_to";
        private String thematicDocumentLinks = "a.link_to[href*=/document/]";
        private String thematicAlternativeDocumentLinks = "div.content a[href*='guid']";
        private String thematicDocumentTitle = "div.item_title, .title, h3, h4";
        private String thematicDocumentDescription = "div.item_description, .description";
        private String thematicContentContainers = "section.layout, div.Section1, div.document-content, div.content, div.text";
        private String thematicAlternativeContentContainers = "div[id^=content], div[class*=content], article, main";
        private String thematicElementsToRemove = "script, link, meta, div.l-main__header, noscript, iframe, div.ya-share2, div.backnav, div.linkcart, header, footer, nav";
    }

    private Selectors selectors = new Selectors();
}