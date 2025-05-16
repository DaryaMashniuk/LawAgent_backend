package by.masnhyuk.lawAgent.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.regex.*;

public class DocumentNumberExtractor {
    private static final Logger log = LogManager.getLogger();
    private static final Pattern COMPLEX_NUMBER_PATTERN =
            Pattern.compile("№\\s*([A-Za-zА-Яа-я-]+)?(\\d+)(?:[-/](\\d+))?(?:-([A-Za-zА-Яа-я]+))?");
    private static final Pattern DIGITS_WITH_HYPHEN = Pattern.compile("(\\d+)-(\\d+)");
    private static final Pattern DATE_PATTERN =
            Pattern.compile("от (\\d{1,2}) (\\p{L}+) (\\d{4})");

    public static Integer extractNumber(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        // Обрабатываем случаи типа "123-456" -> объединяем числа
        Matcher hyphenMatcher = DIGITS_WITH_HYPHEN.matcher(text);
        if (hyphenMatcher.find()) {
            String combined = hyphenMatcher.group(1) + hyphenMatcher.group(2);
            try {
                return Integer.parseInt(combined);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse hyphenated number from: {}", text);
            }
        }

        // 1. Пробуем извлечь сложный номер (типа "З-168/2004" или "56-З")
        Matcher numberMatcher = COMPLEX_NUMBER_PATTERN.matcher(text);
        if (numberMatcher.find()) {
            try {
                String prefix = numberMatcher.group(1);  // "З-" или null
                String mainNum = numberMatcher.group(2); // "168" или "56"
                String year = numberMatcher.group(3);    // "2004" или null
                String suffix = numberMatcher.group(4);  // "З" или null

                // Для формата "З-168/2004" → 1682004
                if (year != null) {
                    return Integer.parseInt(mainNum) * 10000 + Integer.parseInt(year);
                }
                // Для формата "56-З" → 56 (суффикс игнорируется)
                else if (suffix != null) {
                    return Integer.parseInt(mainNum);
                }
                // Простой номер
                else {
                    return Integer.parseInt(mainNum);
                }
            } catch (NumberFormatException e) {
                log.warn("Failed to parse complex number from: {}", text);
            }
        }

        // 2. Если номер не найден, пробуем извлечь дату (ГГГГММДД)
        Matcher dateMatcher = DATE_PATTERN.matcher(text);
        if (dateMatcher.find()) {
            try {
                int day = Integer.parseInt(dateMatcher.group(1));
                String monthStr = dateMatcher.group(2);
                int year = Integer.parseInt(dateMatcher.group(3));

                int month = convertMonthNameToNumber(monthStr);
                return year * 10000 + month * 100 + day;
            } catch (Exception e) {
                log.warn("Failed to parse date from: {}", text, e);
            }
        }

        return null;
    }

    private static int convertMonthNameToNumber(String monthStr) {
        return switch (monthStr.toLowerCase()) {
            case "января" -> 1;
            case "февраля" -> 2;
            case "марта" -> 3;
            case "апреля" -> 4;
            case "мая" -> 5;
            case "июня" -> 6;
            case "июля" -> 7;
            case "августа" -> 8;
            case "сентября" -> 9;
            case "октября" -> 10;
            case "ноября" -> 11;
            case "декабря" -> 12;
            default -> throw new IllegalArgumentException("Unknown month: " + monthStr);
        };
    }
}