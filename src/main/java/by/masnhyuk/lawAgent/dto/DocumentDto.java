package by.masnhyuk.lawAgent.dto;

import by.masnhyuk.lawAgent.entity.DocumentCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class DocumentDto {
    private UUID id;
    private String title;
    private Set<DocumentCategory> categories;
    private DocumentCategory groupCategory;
    private Integer number;
    private String sourceUrl;
    private String details;
    private List<DocumentVersionDto> versions;
}