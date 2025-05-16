package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.ComparisonResult;
import by.masnhyuk.lawAgent.dto.DiffBlock;
import by.masnhyuk.lawAgent.dto.ParagraphPair;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.util.HungarianAlgorithm;
import by.masnhyuk.lawAgent.util.ParagraphMatcher;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentComparisonService {

    private final DocumentVersionRepository versionRepository;
    private static final Logger logger = LogManager.getLogger();
    private static final double SIMILARITY_THRESHOLD = 0.8;

    private final DiffMatchPatch diffMatchPatch = new DiffMatchPatch();

    @Transactional(readOnly = true)
    public ComparisonResult compareVersions(UUID firstVersionId, UUID secondVersionId) {
        DocumentVersion oldVersion = versionRepository.findById(firstVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("First version not found"));
        DocumentVersion newVersion = versionRepository.findById(secondVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Second version not found"));

        List<ParagraphPair> oldParas = extractParagraphs(oldVersion.getContent());
        List<ParagraphPair> newParas = extractParagraphs(newVersion.getContent());

        List<String> oldTexts = oldParas.stream().map(ParagraphPair::plainText).toList();
        List<String> newTexts = newParas.stream().map(ParagraphPair::plainText).toList();

        double[][] simMatrix = ParagraphMatcher.buildSimilarityMatrix(oldTexts, newTexts);

        int size = Math.max(oldTexts.size(), newTexts.size());
        double[][] costMatrix = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i < oldTexts.size() && j < newTexts.size()) {
                    costMatrix[i][j] = 1.0 - simMatrix[i][j];
                } else {
                    costMatrix[i][j] = 1.0;
                }
            }
        }

        HungarianAlgorithm hungarian = new HungarianAlgorithm(costMatrix);
        int[] assignment = hungarian.execute();

        List<DiffBlock> diffBlocks = new ArrayList<>();

        for (int i = 0; i < assignment.length; i++) {
            int j = assignment[i];
            if (i >= oldTexts.size() || j >= newTexts.size()) continue;

            double similarity = simMatrix[i][j];
            if (similarity == 1.0) {
                diffBlocks.add(new DiffBlock(DiffMatchPatch.Operation.EQUAL, oldParas.get(i).rawHtml(), i, j));
            } else if (similarity >= SIMILARITY_THRESHOLD) {
                LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diffMain(
                        oldTexts.get(i), newTexts.get(j));
                diffMatchPatch.diffCleanupSemantic(diffs);
                for (DiffMatchPatch.Diff d : diffs) {
                    // Можно отдать оригинальный HTML от old/new в зависимости от операции
                    String originalHtml = (d.operation == DiffMatchPatch.Operation.INSERT)
                            ? newParas.get(j).rawHtml()
                            : oldParas.get(i).rawHtml();
                    diffBlocks.add(new DiffBlock(d.operation, originalHtml, i, j));
                }
            } else {
                diffBlocks.add(new DiffBlock(DiffMatchPatch.Operation.DELETE, oldParas.get(i).rawHtml(), i, null));
                diffBlocks.add(new DiffBlock(DiffMatchPatch.Operation.INSERT, newParas.get(j).rawHtml(), null, j));
            }
        }

        Set<Integer> matchedNewIndices = Arrays.stream(assignment).boxed().collect(Collectors.toSet());
        for (int j = 0; j < newTexts.size(); j++) {
            if (!matchedNewIndices.contains(j)) {
                diffBlocks.add(new DiffBlock(DiffMatchPatch.Operation.INSERT, newParas.get(j).rawHtml(), null, j));
            }
        }

        List<DiffBlock> realChanges = diffBlocks.stream()
                .filter(b -> b.operation() != DiffMatchPatch.Operation.EQUAL)
                .collect(Collectors.toList());

        return new ComparisonResult(
                realChanges,
                !realChanges.isEmpty(),
                oldVersion.getCreatedAt(),
                newVersion.getCreatedAt(),
                oldVersion.getId(),
                newVersion.getId(),
                oldVersion.getContent(),
                newVersion.getContent()
        );
    }
    private List<ParagraphPair> extractParagraphs(String html) {
        if (html == null || html.isBlank()) return Collections.emptyList();

        String processedHtml = html
                .replaceAll("(?i)<div[^>]*>", "") // Удаляем открывающие теги <div>
                .replaceAll("(?i)</div>", "") // Удаляем закрывающие теги </div>
                .replaceAll("(?i)<\\s*(p|h[1-6])[^>]*>", "\n$0") // Вставляем \n перед открывающими тегами
                .replaceAll("(?i)</\\s*(p|h[1-6])>", "$0\n") // Вставляем \n после закрывающих тегов
//                .replaceAll("&nbsp;", " ") // Заменяем &nbsp; на пробел
                .replaceAll("[ \\t]+", " ") // Убираем лишние пробелы
                .trim();

        String[] rawSplits = processedHtml.split("\n"); // Разделяем по новой строке

        List<ParagraphPair> result = new ArrayList<>();
        for (String raw : rawSplits) {
            String plain = raw.replaceAll("<[^>]+>", "").trim();
            if (!plain.isEmpty()) {
                result.add(new ParagraphPair(raw.trim(), plain));
            }
        }

        return result;
    }
//    private List<ParagraphPair> extractParagraphs(String html) {
//        if (html == null || html.isBlank()) return Collections.emptyList();
//
//        String[] rawSplits = html
//                .replaceAll("(?i)<\\s*(p|div|li|br|h[1-6])[^>]*>", "\n")
//                .replaceAll("(?i)</\\s*(p|div|li|br|h[1-6])>", "")
//                .replaceAll("&nbsp;", " ")
//                .replaceAll("[ \\t]+", " ")
//                .trim()
//                .split("\n");
//
//        List<ParagraphPair> result = new ArrayList<>();
//        for (String raw : rawSplits) {
//            String plain = raw.replaceAll("<[^>]+>", "").trim();
//            if (!plain.isEmpty()) {
//                result.add(new ParagraphPair(raw.trim(), plain));
//            }
//        }
//
//        return result;
//    }

    @Transactional(readOnly = true)
    public List<DocumentVersion> getDocumentVersions(UUID documentId) {
        return versionRepository.findByDocumentIdOrderByNumberDesc(documentId);
    }
}
