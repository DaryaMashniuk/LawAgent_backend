package by.masnhyuk.lawAgent.entity;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Getter
public enum DocumentCategory {
    DECREE("DECREE", "постановление", "указ", "распоряжение"),
    LAW("LAW", "закон", "кодекс"),
    PROGRAM_ACTS("PROGRAM_ACTS", "акты программного характера", "программные акты", "концепция"),
    ADMIN_PROCEDURES("ADMIN_PROCEDURES", "административные процедуры", "административный регламент"),
    BUSINESS("BUSINESS", "бизнес", "предприниматель"),
    STATE_BODIES("STATE_BODIES", "государственный","государственные","государственные органы", "органы", "орган"),
    EAEU_LAW("EAEU_LAW", "ЕАЭС", "Евразийский"),
    COURT_PRACTICE("COURT_PRACTICE", "судебный","судебная практика", "решение суда"),
    RECENT("RECENT"),
    OTHER("OTHER");

    private final String code;
    private final Set<String> keywords;

    DocumentCategory(String code, String... keywords) {
        this.code = code;
        this.keywords = new HashSet<>(Arrays.asList(keywords));
    }

    public boolean matches(String text) {
        if (text == null || text.isEmpty()) return false;
        String lowerText = text.toLowerCase();
        return keywords.stream().anyMatch(keyword -> lowerText.contains(keyword.toLowerCase()));
    }

    public String getTitle() {
        return this.keywords.iterator().next(); // Возвращаем первый ключевой элемент как заголовок
    }

    public static DocumentCategory fromTitle(String title) {
        for (DocumentCategory category : values()) {
            if (category.keywords.contains(title.toLowerCase())) {
                return category;
            }
        }
        return OTHER;
    }

    public static DocumentCategory fromCode(String code) {
        for (DocumentCategory category : values()) {
            if (category.code.equals(code)) {
                return category;
            }
        }
        return OTHER;
    }
}