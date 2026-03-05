import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children;
    boolean isEndOfWord;
    int frequency; // frequency of the query

    TrieNode() {
        children = new HashMap<>();
        isEndOfWord = false;
        frequency = 0;
    }
}

public class AutocompleteSystem {
    private TrieNode root;
    private Map<String, Integer> globalFrequency; // query → frequency

    public AutocompleteSystem() {
        root = new TrieNode();
        globalFrequency = new HashMap<>();
    }

    // Insert query into Trie
    public void insert(String query, int freq) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isEndOfWord = true;
        node.frequency += freq;

        globalFrequency.put(query, globalFrequency.getOrDefault(query, 0) + freq);
    }

    // Update frequency when new search happens
    public void updateFrequency(String query) {
        insert(query, 1);
        System.out.println("updateFrequency(\"" + query + "\") → Frequency: " + globalFrequency.get(query));
    }

    // DFS to collect suggestions
    private void dfs(TrieNode node, String prefix, List<String> results) {
        if (node.isEndOfWord) {
            results.add(prefix);
        }
        for (Map.Entry<Character, TrieNode> entry : node.children.entrySet()) {
            dfs(entry.getValue(), prefix + entry.getKey(), results);
        }
    }

    // Get top K suggestions for a prefix
    public List<String> search(String prefix, int k) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            if (!node.children.containsKey(c)) {
                return Collections.emptyList();
            }
            node = node.children.get(c);
        }

        List<String> candidates = new ArrayList<>();
        dfs(node, prefix, candidates);

        // Sort by frequency (descending)
        candidates.sort((a, b) -> globalFrequency.get(b) - globalFrequency.get(a));

        // Return top K
        List<String> topK = new ArrayList<>();
        for (int i = 0; i < Math.min(k, candidates.size()); i++) {
            String q = candidates.get(i);
            topK.add(q + " (" + globalFrequency.get(q) + " searches)");
        }
        return topK;
    }

    // Demo
    public static void main(String[] args) {
        AutocompleteSystem system = new AutocompleteSystem();

        // Insert sample queries
        system.insert("java tutorial", 1234567);
        system.insert("javascript", 987654);
        system.insert("java download", 456789);
        system.insert("java 21 features", 1);

        // Autocomplete
        System.out.println("search(\"jav\") → ");
        for (String suggestion : system.search("jav", 10)) {
            System.out.println(suggestion);
        }

        // Update frequency
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");
        system.updateFrequency("java 21 features");

        System.out.println("search(\"java 21\") → ");
        for (String suggestion : system.search("java 21", 10)) {
            System.out.println(suggestion);
        }
    }
}
