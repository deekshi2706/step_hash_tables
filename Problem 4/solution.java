import java.util.*;

class PlagiarismDetector {
    // n-gram size (e.g., 5 words)
    private final int N_GRAM_SIZE;
    // n-gram → set of document IDs
    private Map<String, Set<String>> nGramIndex;

    public PlagiarismDetector(int nGramSize) {
        this.N_GRAM_SIZE = nGramSize;
        this.nGramIndex = new HashMap<>();
    }

    // Break document into n-grams
    private List<String> extractNGrams(String[] words) {
        List<String> nGrams = new ArrayList<>();
        for (int i = 0; i <= words.length - N_GRAM_SIZE; i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < N_GRAM_SIZE; j++) {
                sb.append(words[i + j]).append(" ");
            }
            nGrams.add(sb.toString().trim());
        }
        return nGrams;
    }

    // Index a document into the n-gram table
    public void indexDocument(String docId, String text) {
        String[] words = text.split("\\s+");
        List<String> nGrams = extractNGrams(words);

        for (String nGram : nGrams) {
            nGramIndex.computeIfAbsent(nGram, k -> new HashSet<>()).add(docId);
        }
    }

    // Analyze a new document against indexed ones
    public Map<String, Double> analyzeDocument(String docId, String text) {
        String[] words = text.split("\\s+");
        List<String> nGrams = extractNGrams(words);

        Map<String, Integer> matchCounts = new HashMap<>();
        for (String nGram : nGrams) {
            if (nGramIndex.containsKey(nGram)) {
                for (String existingDoc : nGramIndex.get(nGram)) {
                    matchCounts.put(existingDoc, matchCounts.getOrDefault(existingDoc, 0) + 1);
                }
            }
        }

        Map<String, Double> similarityScores = new HashMap<>();
        for (Map.Entry<String, Integer> entry : matchCounts.entrySet()) {
            double similarity = (entry.getValue() * 100.0) / nGrams.size();
            similarityScores.put(entry.getKey(), similarity);
        }

        return similarityScores;
    }

    // Find most similar document
    public String findMostSimilar(Map<String, Double> similarityScores) {
        String mostSimilarDoc = null;
        double maxScore = 0;
        for (Map.Entry<String, Double> entry : similarityScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                mostSimilarDoc = entry.getKey();
            }
        }
        return mostSimilarDoc + " (" + maxScore + "% similarity)";
    }

    // Demo
    public static void main(String[] args) {
        PlagiarismDetector detector = new PlagiarismDetector(5);

        // Index previous essays
        detector.indexDocument("essay_089.txt", "This is a sample essay about computer science and algorithms.");
        detector.indexDocument("essay_092.txt", "Algorithms and data structures are fundamental in computer science.");

        // Analyze new essay
        Map<String, Double> scores = detector.analyzeDocument("essay_123.txt",
                "Computer science is about algorithms and data structures. This essay discusses algorithms.");

        System.out.println("Similarity scores: " + scores);
        System.out.println("Most similar: " + detector.findMostSimilar(scores));
    }
}
