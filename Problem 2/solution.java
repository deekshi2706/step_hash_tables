import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FlashSaleInventoryManager {
    // Product stock levels (thread-safe)
    private Map<String, AtomicInteger> stockMap;
    
    // Waiting list for customers (FIFO order)
    private Map<String, Queue<Integer>> waitingList;

    public FlashSaleInventoryManager() {
        stockMap = new ConcurrentHashMap<>();
        waitingList = new ConcurrentHashMap<>();
    }

    // Initialize product with stock
    public void addProduct(String productId, int stockCount) {
        stockMap.put(productId, new AtomicInteger(stockCount));
        waitingList.put(productId, new LinkedList<>());
    }

    // Check stock availability in O(1)
    public int checkStock(String productId) {
        return stockMap.getOrDefault(productId, new AtomicInteger(0)).get();
    }

    // Process purchase request safely
    public synchronized String purchaseItem(String productId, int userId) {
        AtomicInteger stock = stockMap.get(productId);
        if (stock == null) {
            return "Product not found!";
        }

        if (stock.get() > 0) {
            stock.decrementAndGet();
            return "Success, " + stock.get() + " units remaining";
        } else {
            Queue<Integer> queue = waitingList.get(productId);
            queue.add(userId);
            return "Added to waiting list, position #" + queue.size();
        }
    }

    // Get waiting list for a product
    public List<Integer> getWaitingList(String productId) {
        return new ArrayList<>(waitingList.getOrDefault(productId, new LinkedList<>()));
    }

    // Demo
    public static void main(String[] args) {
        FlashSaleInventoryManager manager = new FlashSaleInventoryManager();

        // Add product with 100 units
        manager.addProduct("IPHONE15_256GB", 100);

        // Check stock
        System.out.println("checkStock(\"IPHONE15_256GB\") → " + manager.checkStock("IPHONE15_256GB"));

        // Simulate purchases
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 67890));

        // Exhaust stock
        for (int i = 0; i < 98; i++) {
            manager.purchaseItem("IPHONE15_256GB", i);
        }

        // Attempt purchase after stock runs out
        System.out.println(manager.purchaseItem("IPHONE15_256GB", 99999));

        // Show waiting list
        System.out.println("Waiting list: " + manager.getWaitingList("IPHONE15_256GB"));
    }
}
