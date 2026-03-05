import java.util.*;

class VideoData {
    String videoId;
    String content; // Simplified: actual video data or metadata
    VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

// LRU Cache using LinkedHashMap
class LRUCache<K, V> extends LinkedHashMap<K, V> {
    private final int capacity;
    public LRUCache(int capacity) {
        super(capacity, 0.75f, true); // access-order
        this.capacity = capacity;
    }
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > capacity;
    }
}

public class MultiLevelCacheSystem {
    private LRUCache<String, VideoData> L1; // Memory cache
    private LRUCache<String, VideoData> L2; // SSD cache
    private Map<String, VideoData> L3;      // Database (slow)

    private int L1Hits = 0, L2Hits = 0, L3Hits = 0;
    private int L1Miss = 0, L2Miss = 0, L3Miss = 0;

    public MultiLevelCacheSystem() {
        L1 = new LRUCache<>(10_000);
        L2 = new LRUCache<>(100_000);
        L3 = new HashMap<>(); // Simulated DB
    }

    // Simulate DB load
    public void addToDatabase(String videoId, String content) {
        L3.put(videoId, new VideoData(videoId, content));
    }

    // Get video from cache hierarchy
    public VideoData getVideo(String videoId) {
        // L1 check
        if (L1.containsKey(videoId)) {
            L1Hits++;
            return L1.get(videoId);
        } else {
            L1Miss++;
        }

        // L2 check
        if (L2.containsKey(videoId)) {
            L2Hits++;
            VideoData data = L2.get(videoId);
            // Promote to L1
            L1.put(videoId, data);
            return data;
        } else {
            L2Miss++;
        }

        // L3 check (DB)
        if (L3.containsKey(videoId)) {
            L3Hits++;
            VideoData data = L3.get(videoId);
            // Add to L2
            L2.put(videoId, data);
            return data;
        } else {
            L3Miss++;
        }

        return null; // Not found
    }

    // Invalidate cache when content updates
    public void invalidate(String videoId) {
        L1.remove(videoId);
        L2.remove(videoId);
        L3.remove(videoId);
    }

    // Statistics
    public void getStatistics() {
        int totalHits = L1Hits + L2Hits + L3Hits;
        int totalMiss = L1Miss + L2Miss + L3Miss;
        int total = totalHits + totalMiss;

        System.out.println("L1: Hit Rate " + percent(L1Hits, total) + "%, Hits: " + L1Hits);
        System.out.println("L2: Hit Rate " + percent(L2Hits, total) + "%, Hits: " + L2Hits);
        System.out.println("L3: Hit Rate " + percent(L3Hits, total) + "%, Hits: " + L3Hits);
        System.out.println("Overall: Hit Rate " + percent(totalHits, total) + "%");
    }

    private double percent(int part, int total) {
        return total == 0 ? 0 : (part * 100.0 / total);
    }

    // Demo
    public static void main(String[] args) {
        MultiLevelCacheSystem cache = new MultiLevelCacheSystem();

        // Populate DB
        cache.addToDatabase("video_123", "Breaking News Video");
        cache.addToDatabase("video_999", "Movie Trailer");

        // First request
        System.out.println("getVideo(\"video_123\") → " + cache.getVideo("video_123").content);
        // Second request (should hit L1)
        System.out.println("getVideo(\"video_123\") → " + cache.getVideo("video_123").content);
        // Another video
        System.out.println("getVideo(\"video_999\") → " + cache.getVideo("video_999").content);

        cache.getStatistics();
    }
}
