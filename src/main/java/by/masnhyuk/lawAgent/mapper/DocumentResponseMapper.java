package by.masnhyuk.lawAgent.mapper;

import by.masnhyuk.lawAgent.dto.DocumentWithVersionsDto;
import by.masnhyuk.lawAgent.dto.FullDocumentVersionDto;
import by.masnhyuk.lawAgent.dto.VersionShortDto;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentResponseMapper {
    public static DocumentWithVersionsDto toDto(DocumentEntity entity) {
        List<VersionShortDto> versionDtos = entity.getVersions().stream()
                .map(version -> new VersionShortDto(
                        version.getId(),
                        version.getCreatedAt(),
                        version.getSourceUrl(),
                        version.getDetails(),
                        version.getNumber()
                ))
                .collect(Collectors.toList());

        return new DocumentWithVersionsDto(
                entity.getId(),
                entity.getTitle(),
                versionDtos
        );
    }


    public static FullDocumentVersionDto convertToFullVersionDto(DocumentVersion version) {
        FullDocumentVersionDto dto = new FullDocumentVersionDto();
        dto.setId(version.getId());
        dto.setTitle(version.getDocument().getTitle());
        dto.setContent(version.getContent());
        dto.setPdfContent(version.getPdfContent());
        dto.setCreatedAt(version.getCreatedAt());
        dto.setNumber(version.getNumber());
        dto.setSourceUrl(version.getSourceUrl());
        dto.setDetails(version.getDetails());
        dto.setMainDocumentId(version.getDocument().getId());
        return dto;
    }

}
