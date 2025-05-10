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

        patterns.put("DECREE", new String[]{"Постановление", "Указ", "Распоряжение"});
        patterns.put("LAW", new String[]{"Закон", "Кодекс"});
        patterns.put("PROGRAM_ACTS", new String[]{"Программа", "Концепция"});
        patterns.put("ADMIN_PROCEDURES", new String[]{"Административный", "Процедура"});
        patterns.put("BUSINESS", new String[]{"Бизнес", "Предприниматель"});
        patterns.put("STATE_BODIES", new String[]{"Государственный", "Орган"});
        patterns.put("EAEU_LAW", new String[]{"ЕАЭС", "Евразийский"});
        patterns.put("COURT_PRACTICE", new String[]{"Судебный", "Решение суда"});
    }
}