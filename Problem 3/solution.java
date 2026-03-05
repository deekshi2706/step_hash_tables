import java.util.*;
import java.util.concurrent.*;

class DNSEntry {
    String domain;
    String ipAddress;
    long expiryTime; // in milliseconds

    DNSEntry(String domain, String ipAddress, int ttlSeconds) {
        this.domain = domain;
        this.ipAddress = ipAddress;
        this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

public class DNSCache {
    private final int MAX_CACHE_SIZE;
    private final Map<String, DNSEntry> cache;
    private int hits = 0;
    private int misses = 0;

    public DNSCache(int maxSize) {
        this.MAX_CACHE_SIZE = maxSize;
        // LinkedHashMap with access-order = true → LRU eviction
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > MAX_CACHE_SIZE;
            }
        };

        // Background thread to clean expired entries
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            synchronized (cache) {
                Iterator<Map.Entry<String, DNSEntry>> it = cache.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, DNSEntry> entry = it.next();
                    if (entry.getValue().isExpired()) {
                        it.remove();
                    }
                }
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    // Resolve domain → IP
    public synchronized String resolve(String domain) {
        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            return "Cache HIT → " + entry.ipAddress;
        } else {
            misses++;
            // Simulate upstream DNS query (random IP for demo)
            String ipAddress = "172.217.14." + new Random().nextInt(255);
            int ttl = 300; // TTL in seconds
            cache.put(domain, new DNSEntry(domain, ipAddress, ttl));
            return "Cache MISS → Query upstream → " + ipAddress + " (TTL: " + ttl + "s)";
        }
    }

    // Cache statistics
    public String getCacheStats() {
        int total = hits + misses;
        double hitRate = total == 0 ? 0 : (hits * 100.0 / total);
        return String.format("Hit Rate: %.2f%%, Hits: %d, Misses: %d", hitRate, hits, misses);
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        DNSCache dnsCache = new DNSCache(5);

        System.out.println(dnsCache.resolve("google.com")); // MISS
        System.out.println(dnsCache.resolve("google.com")); // HIT

        Thread.sleep(310 * 1000); // wait for TTL expiry (simulate)
        System.out.println(dnsCache.resolve("google.com")); // EXPIRED → MISS

        System.out.println(dnsCache.getCacheStats());
    }
}
