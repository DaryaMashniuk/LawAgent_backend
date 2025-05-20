package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.DocumentDto;
import by.masnhyuk.lawAgent.dto.DocumentWithVersionsDto;
import by.masnhyuk.lawAgent.dto.FullDocumentVersionDto;
import by.masnhyuk.lawAgent.entity.DocumentCategory;
import by.masnhyuk.lawAgent.entity.DocumentEntity;
import by.masnhyuk.lawAgent.mapper.DocumentMapper;
import by.masnhyuk.lawAgent.mapper.DocumentResponseMapper;
import by.masnhyuk.lawAgent.repository.DocumentRepository;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.service.DocumentsService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DocumentsServiceImpl implements DocumentsService {
    public DocumentMapper documentMapper;

    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository documentVersionRepository;

    @Override
    public List<DocumentEntity> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .toList();
    }

    @Override
    public Optional<DocumentDto> getDocumentById(UUID documentId) {
        return documentRepository.findById(documentId)
                .map(DocumentMapper::mapToDto);
    }

    @Override
    public Page<DocumentWithVersionsDto> searchDocuments(String query, Set<DocumentCategory> categories, Pageable pageable) {
        Page<DocumentEntity> docsPage = documentRepository.searchByQueryAndCategories(query, categories, pageable);

        return docsPage.map(DocumentResponseMapper::toDto);
    }

    @Override
    public Optional<FullDocumentVersionDto> getDocumentVersionById(UUID versionId) {
        return documentVersionRepository.findById(versionId)
                .map(DocumentResponseMapper::convertToFullVersionDto);
    }

}
