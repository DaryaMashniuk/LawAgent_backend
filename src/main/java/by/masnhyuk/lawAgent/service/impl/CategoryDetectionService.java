package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.config.CategoryDetectionProperties;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CategoryDetectionService {
    private final CategoryDetectionProperties properties;

    public Set<DocumentCategory> detectCategories(String text) {
        Set<DocumentCategory> categories = new HashSet<>();

        if (text == null || text.isBlank()) {
            return Set.of(DocumentCategory.OTHER);
        }

        String lowerText = text.toLowerCase();

        for (DocumentCategory category : DocumentCategory.values()) {
            if (category == DocumentCategory.OTHER) continue;

            for (String keyword : category.getKeywords()) {
                if (lowerText.contains(keyword.toLowerCase())) {
                    categories.add(category);
                    break;
                }
            }
        }

        return categories.isEmpty() ? Set.of(DocumentCategory.OTHER) : categories;
    }
}