package by.masnhyuk.lawAgent.controller;

import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.service.impl.PravoParserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ParserController {
    private final PravoParserServiceImpl parserService;
    private final DocumentVersionRepository versionRepo;
    private static final Logger log = LogManager.getLogger();
    @PostMapping("/run")
    public ResponseEntity<String> runParserManually() {
        try {
            parserService.manualParseNewDocuments();
            return ResponseEntity.ok("Parser started successfully");
        } catch (Exception e) {
            log.error("Manual parser failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Parser failed: " + e.getMessage());
        }
    }


}
