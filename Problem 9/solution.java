import java.util.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    long timestamp; // epoch ms

    Transaction(int id, int amount, String merchant, String account, String time) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.timestamp = parseTime(time);
    }

    private long parseTime(String time) {
        // Simplified: HH:mm → epoch minutes
        String[] parts = time.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return (hours * 60 + minutes) * 60 * 1000L;
    }
}

public class TransactionAnalyzer {
    private List<Transaction> transactions;

    public TransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Classic Two-Sum
    public List<String> findTwoSum(int target) {
        Map<Integer, Transaction> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add("(" + map.get(complement).id + ", " + t.id + ")");
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum with 1-hour window
    public List<String> findTwoSumWithTimeWindow(int target) {
        List<String> result = new ArrayList<>();
        Map<Integer, List<Transaction>> map = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction other : map.get(complement)) {
                    if (Math.abs(t.timestamp - other.timestamp) <= 3600_000) {
                        result.add("(" + other.id + ", " + t.id + ")");
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // K-Sum (recursive)
    public List<List<Integer>> findKSum(int k, int target) {
        List<List<Integer>> result = new ArrayList<>();
        backtrack(transactions, k, target, 0, new ArrayList<>(), result);
        return result;
    }

    private void backtrack(List<Transaction> txs, int k, int target, int start,
                           List<Integer> current, List<List<Integer>> result) {
        if (k == 0 && target == 0) {
            result.add(new ArrayList<>(current));
            return;
        }
        if (k == 0 || target < 0) return;

        for (int i = start; i < txs.size(); i++) {
            current.add(txs.get(i).id);
            backtrack(txs, k - 1, target - txs.get(i).amount, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    // Duplicate detection
    public List<String> detectDuplicates() {
        Map<String, Map<Integer, Set<String>>> map = new HashMap<>();
        List<String> result = new ArrayList<>();

        for (Transaction t : transactions) {
            map.putIfAbsent(t.merchant, new HashMap<>());
            Map<Integer, Set<String>> amountMap = map.get(t.merchant);

            amountMap.putIfAbsent(t.amount, new HashSet<>());
            Set<String> accounts = amountMap.get(t.amount);

            if (accounts.contains(t.account)) continue;
            if (!accounts.isEmpty()) {
                result.add("{amount:" + t.amount + ", merchant:" + t.merchant +
                        ", accounts:" + accounts + " & " + t.account + "}");
            }
            accounts.add(t.account);
        }
        return result;
    }

    // Demo
    public static void main(String[] args) {
        List<Transaction> txs = Arrays.asList(
                new Transaction(1, 500, "Store A", "acc1", "10:00"),
                new Transaction(2, 300, "Store B", "acc2", "10:15"),
                new Transaction(3, 200, "Store C", "acc3", "10:30"),
                new Transaction(4, 500, "Store A", "acc2", "11:00")
        );

        TransactionAnalyzer analyzer = new TransactionAnalyzer(txs);

        System.out.println("findTwoSum(target=500) → " + analyzer.findTwoSum(500));
        System.out.println("findTwoSumWithTimeWindow(target=500) → " + analyzer.findTwoSumWithTimeWindow(500));
        System.out.println("findKSum(k=3, target=1000) → " + analyzer.findKSum(3, 1000));
        System.out.println("detectDuplicates() → " + analyzer.detectDuplicates());
    }
}
