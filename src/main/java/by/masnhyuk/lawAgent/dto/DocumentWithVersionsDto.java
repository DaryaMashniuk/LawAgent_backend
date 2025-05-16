package by.masnhyuk.lawAgent.dto;

import by.masnhyuk.lawAgent.entity.DocumentVersion;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class DocumentWithVersionsDto {
    private UUID documentId;
    private String title;
    private List<VersionShortDto> versions;
}
