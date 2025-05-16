package by.masnhyuk.lawAgent.dto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
public class DocumentVersionDto {
    private UUID id;
    private String content;
    private byte[] pdfContent;
    private String contentHash;
    private String pdfContentHash;
    private LocalDateTime createdAt;
    private Integer number;
    private String sourceUrl;
    private String details;
}