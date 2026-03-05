import java.util.*;

public class UsernameChecker {
    // HashMap for username -> userId mapping
    private Map<String, Integer> usernames;
    
    // HashMap for attempted username frequency
    private Map<String, Integer> attemptFrequency;

    public UsernameChecker() {
        usernames = new HashMap<>();
        attemptFrequency = new HashMap<>();
    }

    // Register a new user if username is available
    public boolean registerUser(String username, int userId) {
        if (usernames.containsKey(username)) {
            return false; // already taken
        }
        usernames.put(username, userId);
        return true;
    }

    // Check availability in O(1)
    public boolean checkAvailability(String username) {
        attemptFrequency.put(username, attemptFrequency.getOrDefault(username, 0) + 1);
        return !usernames.containsKey(username);
    }

    // Suggest alternatives if username is taken
    public List<String> suggestAlternatives(String username, int numSuggestions) {
        List<String> suggestions = new ArrayList<>();
        int i = 1;
        while (suggestions.size() < numSuggestions) {
            String alt = username + i;
            if (!usernames.containsKey(alt)) {
                suggestions.add(alt);
            }
            i++;
        }
        // Add a variation with a dot if available
        String altDot = username.replace("_", ".");
        if (!usernames.containsKey(altDot) && suggestions.size() < numSuggestions) {
            suggestions.add(altDot);
        }
        return suggestions;
    }

    // Get most attempted username
    public String getMostAttempted() {
        String mostAttempted = null;
        int maxAttempts = 0;
        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {
            if (entry.getValue() > maxAttempts) {
                maxAttempts = entry.getValue();
                mostAttempted = entry.getKey();
            }
        }
        return mostAttempted + " (" + maxAttempts + " attempts)";
    }

    // Demo
    public static void main(String[] args) {
        UsernameChecker checker = new UsernameChecker();

        // Register some users
        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);

        // Check availability
        System.out.println("checkAvailability(\"john_doe\") → " + checker.checkAvailability("john_doe"));
        System.out.println("checkAvailability(\"jane_smith\") → " + checker.checkAvailability("jane_smith"));

        // Suggest alternatives
        System.out.println("suggestAlternatives(\"john_doe\") → " + checker.suggestAlternatives("john_doe", 3));

        // Track most attempted
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        System.out.println("getMostAttempted() → " + checker.getMostAttempted());
    }
}
