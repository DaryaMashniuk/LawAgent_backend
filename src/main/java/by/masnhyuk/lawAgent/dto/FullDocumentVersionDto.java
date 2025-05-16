package by.masnhyuk.lawAgent.dto;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FullDocumentVersionDto {
    private UUID id;
    private String title;
    private String content;
    private byte[] pdfContent;
    private LocalDateTime createdAt;
    private Integer number;
    private String sourceUrl;
    private String details;
    private UUID MainDocumentId;
}
