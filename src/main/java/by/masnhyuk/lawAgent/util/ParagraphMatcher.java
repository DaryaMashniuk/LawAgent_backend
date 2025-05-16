package by.masnhyuk.lawAgent.util;

import java.util.List;

public class ParagraphMatcher {

    public static double[][] buildSimilarityMatrix(List<String> oldParas, List<String> newParas) {
        double[][] matrix = new double[oldParas.size()][newParas.size()];
        for (int i = 0; i < oldParas.size(); i++) {
            for (int j = 0; j < newParas.size(); j++) {
                matrix[i][j] = calculateSimilarity(oldParas.get(i), newParas.get(j));
            }
        }
        return matrix;
    }

    private static double calculateSimilarity(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) return 0;
        if (a.equals(b)) return 1.0;
        int maxLen = Math.max(a.length(), b.length());
        int distance = levenshteinDistance(a, b);
        return 1.0 - (double) distance / maxLen;
    }

    private static int levenshteinDistance(String s, String t) {
        int[] dp = new int[t.length() + 1];
        for (int j = 0; j <= t.length(); j++) dp[j] = j;
        for (int i = 1; i <= s.length(); i++) {
            int prev = i - 1;
            dp[0] = i;
            for (int j = 1; j <= t.length(); j++) {
                int temp = dp[j];
                dp[j] = Math.min(Math.min(dp[j] + 1, dp[j - 1] + 1), prev + (s.charAt(i - 1) == t.charAt(j - 1) ? 0 : 1));
                prev = temp;
            }
        }
        return dp[t.length()];
    }
}

