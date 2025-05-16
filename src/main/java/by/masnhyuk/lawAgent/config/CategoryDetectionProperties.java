package by.masnhyuk.lawAgent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "parser.categories")
public class CategoryDetectionProperties {
    private Map<String, String[]> patterns = new HashMap<>();

    public CategoryDetectionProperties() {

        patterns.put("DECREE", new String[]{"постановление", "указ", "распоряжение"});
        patterns.put("LAW", new String[]{"закон", "кодекс"});
        patterns.put("PROGRAM_ACTS", new String[]{"акты программного характера", "программные акты", "концепция"});
        patterns.put("ADMIN_PROCEDURES", new String[]{"административные процедуры", "административный регламент"});
        patterns.put("BUSINESS", new String[]{"Бизнес", "Предприниматель"});
        patterns.put("STATE_BODIES", new String[]{"государственный","государственные","государственные органы", "органы", "орган"});
        patterns.put("EAEU_LAW", new String[]{"ЕАЭС", "Евразийский"});
        patterns.put("COURT_PRACTICE", new String[]{"судебный","судебная практика", "решение суда"});
    }
}