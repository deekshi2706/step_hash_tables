import java.util.*;
import java.util.concurrent.*;

class PageStats {
    int visitCount;
    Set<String> uniqueVisitors;

    PageStats() {
        this.visitCount = 0;
        this.uniqueVisitors = new HashSet<>();
    }

    void addVisit(String userId) {
        visitCount++;
        uniqueVisitors.add(userId);
    }

    int getUniqueCount() {
        return uniqueVisitors.size();
    }
}

public class RealTimeAnalytics {
    private Map<String, PageStats> pageViews;
    private Map<String, Integer> trafficSources;

    public RealTimeAnalytics() {
        pageViews = new ConcurrentHashMap<>();
        trafficSources = new ConcurrentHashMap<>();
    }

    // Process incoming event
    public void processEvent(String url, String userId, String source) {
        // Update page stats
        pageViews.putIfAbsent(url, new PageStats());
        pageViews.get(url).addVisit(userId);

        // Update traffic source counts
        trafficSources.put(source, trafficSources.getOrDefault(source, 0) + 1);
    }

    // Get top N pages
    public List<String> getTopPages(int n) {
        PriorityQueue<Map.Entry<String, PageStats>> pq =
                new PriorityQueue<>((a, b) -> b.getValue().visitCount - a.getValue().visitCount);

        pq.addAll(pageViews.entrySet());

        List<String> topPages = new ArrayList<>();
        for (int i = 0; i < n && !pq.isEmpty(); i++) {
            Map.Entry<String, PageStats> entry = pq.poll();
            topPages.add(entry.getKey() + " - " + entry.getValue().visitCount +
                    " views (" + entry.getValue().getUniqueCount() + " unique)");
        }
        return topPages;
    }

    // Get traffic source distribution
    public Map<String, String> getTrafficSourceDistribution() {
        Map<String, String> distribution = new HashMap<>();
        int total = trafficSources.values().stream().mapToInt(Integer::intValue).sum();

        for (Map.Entry<String, Integer> entry : trafficSources.entrySet()) {
            double percentage = (entry.getValue() * 100.0) / total;
            distribution.put(entry.getKey(), String.format("%.1f%%", percentage));
        }
        return distribution;
    }

    // Dashboard update
    public void getDashboard() {
        System.out.println("Top Pages:");
        for (String page : getTopPages(10)) {
            System.out.println(page);
        }

        System.out.println("\nTraffic Sources:");
        Map<String, String> sources = getTrafficSourceDistribution();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    // Demo with scheduled updates
    public static void main(String[] args) {
        RealTimeAnalytics analytics = new RealTimeAnalytics();

        // Simulate events
        analytics.processEvent("/article/breaking-news", "user_123", "google");
        analytics.processEvent("/article/breaking-news", "user_456", "facebook");
        analytics.processEvent("/sports/championship", "user_789", "direct");
        analytics.processEvent("/sports/championship", "user_123", "google");

        // Schedule dashboard updates every 5 seconds
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("\n--- Dashboard Update ---");
            analytics.getDashboard();
        }, 0, 5, TimeUnit.SECONDS);
    }
}
