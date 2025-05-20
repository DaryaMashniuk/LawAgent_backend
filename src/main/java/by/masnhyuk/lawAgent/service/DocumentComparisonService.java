package by.masnhyuk.lawAgent.service;

import by.masnhyuk.lawAgent.dto.ComparisonResult;
import by.masnhyuk.lawAgent.dto.DiffBlock;
import by.masnhyuk.lawAgent.dto.ParagraphPair;
import by.masnhyuk.lawAgent.entity.DocumentVersion;
import by.masnhyuk.lawAgent.service.impl.DocumentComparisonServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface DocumentComparisonService {
    @Transactional(readOnly = true)
    ComparisonResult compareVersions(UUID firstVersionId, UUID secondVersionId);

    DocumentComparisonServiceImpl.DocumentVersions fetchDocumentVersions(UUID firstVersionId, UUID secondVersionId);

    DocumentComparisonServiceImpl.ParagraphLists extractParagraphsFromVersions(DocumentComparisonServiceImpl.DocumentVersions versions);

    double[][] calculateSimilarityMatrix(DocumentComparisonServiceImpl.ParagraphLists paragraphs);

    int[] findOptimalAssignment(DocumentComparisonServiceImpl.ParagraphLists paragraphs, double[][] similarityMatrix);

    double[][] buildCostMatrix(double[][] similarityMatrix, int oldSize, int newSize, int totalSize);

    List<DiffBlock> calculateDifferences(DocumentComparisonServiceImpl.ParagraphLists paragraphs,
                                         double[][] similarityMatrix,
                                         int[] assignment);

    void processAssignedParagraphs(DocumentComparisonServiceImpl.ParagraphLists paragraphs,
                                   double[][] similarityMatrix,
                                   int[] assignment,
                                   List<DiffBlock> diffBlocks,
                                   List<String> oldTexts,
                                   List<String> newTexts);

    void processUnassignedNewParagraphs(DocumentComparisonServiceImpl.ParagraphLists paragraphs,
                                        int[] assignment,
                                        List<DiffBlock> diffBlocks,
                                        int newTextsSize);

    DiffBlock createEqualDiffBlock(ParagraphPair paragraph, int oldIndex, int newIndex);

    List<DiffBlock> createModifiedDiffBlocks(DocumentComparisonServiceImpl.ParagraphLists paragraphs,
                                             int oldIndex,
                                             int newIndex,
                                             String oldText,
                                             String newText);

    DiffBlock createDeleteDiffBlock(ParagraphPair paragraph, int oldIndex);

    DiffBlock createInsertDiffBlock(ParagraphPair paragraph, int newIndex);

    List<DiffBlock> filterRealChanges(List<DiffBlock> diffBlocks);

    ComparisonResult buildComparisonResult(DocumentComparisonServiceImpl.DocumentVersions versions, List<DiffBlock> realChanges);

    @Transactional(readOnly = true)
    List<DocumentVersion> getDocumentVersions(UUID documentId);
}
