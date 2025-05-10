package by.masnhyuk.lawAgent.dto;

import by.masnhyuk.lawAgent.entity.DocumentCategory;

public class ThematicCategory {
    public final String url;
    public final DocumentCategory type;

    public ThematicCategory(String url, DocumentCategory type) {
        this.url = url;
        this.type = type;
    }
}