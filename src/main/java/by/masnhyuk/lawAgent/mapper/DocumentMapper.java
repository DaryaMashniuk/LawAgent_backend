package by.masnhyuk.lawAgent.mapper;

import by.masnhyuk.lawAgent.dto.DocumentDto;
import by.masnhyuk.lawAgent.dto.DocumentVersionDto;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DocumentMapper {

    public static DocumentDto mapToDto(DocumentEntity document) {
        DocumentDto dto = new DocumentDto();
        dto.setId(document.getId());
        dto.setTitle(document.getTitle());
        dto.setNumber(document.getNumber());
        dto.setCategories(document.getCategories());
        dto.setGroupCategory(document.getGroupCategory());
        dto.setSourceUrl(document.getSourceUrl());
        dto.setDetails(document.getDetails());

        dto.setVersions(document.getVersions().stream()
                .map(DocumentMapper::mapVersionToDto)
                .collect(Collectors.toList()));

        return dto;
    }

    public static DocumentVersionDto mapVersionToDto(DocumentVersion version) {
        DocumentVersionDto dto = new DocumentVersionDto();
        dto.setId(version.getId());
        dto.setContent(version.getContent());
        dto.setPdfContent(version.getPdfContent());
        dto.setContentHash(version.getContentHash());
        dto.setPdfContentHash(version.getPdfContentHash());
        dto.setCreatedAt(version.getCreatedAt());
        return dto;
    }
}

