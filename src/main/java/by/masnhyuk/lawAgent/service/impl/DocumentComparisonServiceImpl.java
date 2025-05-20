package by.masnhyuk.lawAgent.service.impl;

import by.masnhyuk.lawAgent.dto.ComparisonResult;
import by.masnhyuk.lawAgent.dto.DiffBlock;
import by.masnhyuk.lawAgent.dto.ParagraphPair;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.repository.DocumentVersionRepository;
import by.masnhyuk.lawAgent.service.DocumentComparisonService;
import by.masnhyuk.lawAgent.util.HtmlParagraphExtractor;
import by.masnhyuk.lawAgent.util.HungarianAlgorithm;
import by.masnhyuk.lawAgent.util.ParagraphMatcher;
import lombok.RequiredArgsConstructor;
import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentComparisonServiceImpl implements DocumentComparisonService {

    private final DocumentVersionRepository versionRepository;
    private static final double SIMILARITY_THRESHOLD = 0.8;
    private final DiffMatchPatch diffMatchPatch = new DiffMatchPatch();

    @Transactional(readOnly = true)
    @Override
    public ComparisonResult compareVersions(UUID firstVersionId, UUID secondVersionId) {
        DocumentVersions versions = fetchDocumentVersions(firstVersionId, secondVersionId);
        ParagraphLists paragraphs = extractParagraphsFromVersions(versions);
        double[][] similarityMatrix = calculateSimilarityMatrix(paragraphs);
        int[] assignment = findOptimalAssignment(paragraphs, similarityMatrix);
        List<DiffBlock> diffBlocks = calculateDifferences(paragraphs, similarityMatrix, assignment);

        return buildComparisonResult(versions, filterRealChanges(diffBlocks));
    }

    @Override
    public DocumentVersions fetchDocumentVersions(UUID firstVersionId, UUID secondVersionId) {
        DocumentVersion oldVersion = versionRepository.findById(firstVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("First version not found"));
        DocumentVersion newVersion = versionRepository.findById(secondVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Second version not found"));
        return new DocumentVersions(oldVersion, newVersion);
    }

    @Override
    public ParagraphLists extractParagraphsFromVersions(DocumentVersions versions) {
        List<ParagraphPair> oldParas = HtmlParagraphExtractor.extractParagraphs(versions.oldVersion().getContent());
        List<ParagraphPair> newParas = HtmlParagraphExtractor.extractParagraphs(versions.newVersion().getContent());
        return new ParagraphLists(oldParas, newParas);
    }

    @Override
    public double[][] calculateSimilarityMatrix(ParagraphLists paragraphs) {
        List<String> oldTexts = paragraphs.oldParas().stream().map(ParagraphPair::plainText).toList();
        List<String> newTexts = paragraphs.newParas().stream().map(ParagraphPair::plainText).toList();
        return ParagraphMatcher.buildSimilarityMatrix(oldTexts, newTexts);
    }

    @Override
    public int[] findOptimalAssignment(ParagraphLists paragraphs, double[][] similarityMatrix) {
        List<String> oldTexts = paragraphs.oldParas().stream().map(ParagraphPair::plainText).toList();
        List<String> newTexts = paragraphs.newParas().stream().map(ParagraphPair::plainText).toList();

        int size = Math.max(oldTexts.size(), newTexts.size());
        double[][] costMatrix = buildCostMatrix(similarityMatrix, oldTexts.size(), newTexts.size(), size);

        return new HungarianAlgorithm(costMatrix).execute();
    }

    @Override
    public double[][] buildCostMatrix(double[][] similarityMatrix, int oldSize, int newSize, int totalSize) {
        double[][] costMatrix = new double[totalSize][totalSize];
        for (int i = 0; i < totalSize; i++) {
            for (int j = 0; j < totalSize; j++) {
                costMatrix[i][j] = (i < oldSize && j < newSize) ? 1.0 - similarityMatrix[i][j] : 1.0;
            }
        }
        return costMatrix;
    }

    @Override
    public List<DiffBlock> calculateDifferences(ParagraphLists paragraphs,
                                                double[][] similarityMatrix,
                                                int[] assignment) {
        List<DiffBlock> diffBlocks = new ArrayList<>();
        List<String> oldTexts = paragraphs.oldParas().stream().map(ParagraphPair::plainText).toList();
        List<String> newTexts = paragraphs.newParas().stream().map(ParagraphPair::plainText).toList();

        processAssignedParagraphs(paragraphs, similarityMatrix, assignment, diffBlocks, oldTexts, newTexts);
        processUnassignedNewParagraphs(paragraphs, assignment, diffBlocks, newTexts.size());

        return diffBlocks;
    }

    @Override
    public void processAssignedParagraphs(ParagraphLists paragraphs,
                                          double[][] similarityMatrix,
                                          int[] assignment,
                                          List<DiffBlock> diffBlocks,
                                          List<String> oldTexts,
                                          List<String> newTexts) {
        for (int i = 0; i < assignment.length; i++) {
            int j = assignment[i];
            if (i >= oldTexts.size() || j >= newTexts.size()) continue;

            double similarity = similarityMatrix[i][j];
            if (similarity == 1.0) {
                diffBlocks.add(createEqualDiffBlock(paragraphs.oldParas().get(i), i, j));
            } else if (similarity >= SIMILARITY_THRESHOLD) {
                diffBlocks.addAll(createModifiedDiffBlocks(paragraphs, i, j, oldTexts.get(i), newTexts.get(j)));
            } else {
                diffBlocks.add(createDeleteDiffBlock(paragraphs.oldParas().get(i), i));
                diffBlocks.add(createInsertDiffBlock(paragraphs.newParas().get(j), j));
            }
        }
    }

    @Override
    public void processUnassignedNewParagraphs(ParagraphLists paragraphs,
                                               int[] assignment,
                                               List<DiffBlock> diffBlocks,
                                               int newTextsSize) {
        Set<Integer> matchedNewIndices = Arrays.stream(assignment).boxed().collect(Collectors.toSet());
        for (int j = 0; j < newTextsSize; j++) {
            if (!matchedNewIndices.contains(j)) {
                diffBlocks.add(createInsertDiffBlock(paragraphs.newParas().get(j), j));
            }
        }
    }

    @Override
    public DiffBlock createEqualDiffBlock(ParagraphPair paragraph, int oldIndex, int newIndex) {
        return new DiffBlock(DiffMatchPatch.Operation.EQUAL, paragraph.rawHtml(), oldIndex, newIndex);
    }

    @Override
    public List<DiffBlock> createModifiedDiffBlocks(ParagraphLists paragraphs,
                                                    int oldIndex,
                                                    int newIndex,
                                                    String oldText,
                                                    String newText) {
        LinkedList<DiffMatchPatch.Diff> diffs = diffMatchPatch.diffMain(oldText, newText);
        diffMatchPatch.diffCleanupSemantic(diffs);

        List<DiffBlock> blocks = new ArrayList<>();
        for (DiffMatchPatch.Diff d : diffs) {
            String originalHtml = (d.operation == DiffMatchPatch.Operation.INSERT)
                    ? paragraphs.newParas().get(newIndex).rawHtml()
                    : paragraphs.oldParas().get(oldIndex).rawHtml();
            blocks.add(new DiffBlock(d.operation, originalHtml, oldIndex, newIndex));
        }
        return blocks;
    }

    @Override
    public DiffBlock createDeleteDiffBlock(ParagraphPair paragraph, int oldIndex) {
        return new DiffBlock(DiffMatchPatch.Operation.DELETE, paragraph.rawHtml(), oldIndex, null);
    }

    @Override
    public DiffBlock createInsertDiffBlock(ParagraphPair paragraph, int newIndex) {
        return new DiffBlock(DiffMatchPatch.Operation.INSERT, paragraph.rawHtml(), null, newIndex);
    }

    @Override
    public List<DiffBlock> filterRealChanges(List<DiffBlock> diffBlocks) {
        return diffBlocks.stream()
                .filter(b -> b.operation() != DiffMatchPatch.Operation.EQUAL)
                .collect(Collectors.toList());
    }

    @Override
    public ComparisonResult buildComparisonResult(DocumentVersions versions, List<DiffBlock> realChanges) {
        return new ComparisonResult(
                realChanges,
                !realChanges.isEmpty(),
                versions.oldVersion().getCreatedAt(),
                versions.newVersion().getCreatedAt(),
                versions.oldVersion().getId(),
                versions.newVersion().getId(),
                versions.oldVersion().getContent(),
                versions.newVersion().getContent()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public List<DocumentVersion> getDocumentVersions(UUID documentId) {
        return versionRepository.findByDocumentIdOrderByNumberDesc(documentId);
    }

    public record DocumentVersions(DocumentVersion oldVersion, DocumentVersion newVersion) {}
    public record ParagraphLists(List<ParagraphPair> oldParas, List<ParagraphPair> newParas) {}
}