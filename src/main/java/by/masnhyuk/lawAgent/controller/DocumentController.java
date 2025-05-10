package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentVersionRepository versionRepo;

//    @GetMapping("/{id}/text")
//    public ResponseEntity<String> getDocumentText(@PathVariable UUID id) {
//        DocumentVersion version = versionRepo.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.TEXT_PLAIN)
//                .body(version.getContent());
//    }
//
//    @GetMapping("/{id}/pdf")
//    public ResponseEntity<byte[]> getDocumentPdf(@PathVariable UUID id) {
//        DocumentVersion version = versionRepo.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Document version not found"));
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_PDF)
//                .header("Content-Disposition", "inline; filename=\"document.pdf\"")
//                .body(version.getPdfContent());
//    }
}