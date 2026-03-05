import java.util.concurrent.*;
import java.util.*;

class TokenBucket {
    private final int maxTokens;
    private final int refillRate; // tokens per hour
    private int tokens;
    private long lastRefillTime;

    public TokenBucket(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
    }

    // Refill tokens based on elapsed time
    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        long hoursPassed = elapsed / (1000 * 60 * 60);

        if (hoursPassed >= 1) {
            tokens = maxTokens; // reset every hour
            lastRefillTime = now;
        }
    }

    public synchronized boolean allowRequest() {
        refill();
        if (tokens > 0) {
            tokens--;
            return true;
        }
        return false;
    }

    public synchronized int getRemainingTokens() {
        refill();
        return tokens;
    }

    public synchronized long getResetTime() {
        return lastRefillTime + (1000 * 60 * 60);
    }

    public int getMaxTokens() {
        return maxTokens;
    }
}

public class DistributedRateLimiter {
    private final Map<String, TokenBucket> clientBuckets;
    private final int maxTokens;
    private final int refillRate;

    public DistributedRateLimiter(int maxTokens, int refillRate) {
        this.maxTokens = maxTokens;
        this.refillRate = refillRate;
        this.clientBuckets = new ConcurrentHashMap<>();
    }

    private TokenBucket getBucket(String clientId) {
        return clientBuckets.computeIfAbsent(clientId, k -> new TokenBucket(maxTokens, refillRate));
    }

    public synchronized String checkRateLimit(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            long retryAfter = (bucket.getResetTime() - System.currentTimeMillis()) / 1000;
            return "Denied (0 requests remaining, retry after " + retryAfter + "s)";
        }
    }

    public synchronized String getRateLimitStatus(String clientId) {
        TokenBucket bucket = getBucket(clientId);
        int used = bucket.getMaxTokens() - bucket.getRemainingTokens();
        return "{used: " + used + ", limit: " + bucket.getMaxTokens() +
                ", reset: " + bucket.getResetTime() + "}";
    }

    // Demo
    public static void main(String[] args) {
        DistributedRateLimiter limiter = new DistributedRateLimiter(1000, 1000);

        String clientId = "abc123";
        for (int i = 0; i < 1002; i++) {
            System.out.println("checkRateLimit(" + clientId + ") → " + limiter.checkRateLimit(clientId));
        }

        System.out.println("getRateLimitStatus(" + clientId + ") → " + limiter.getRateLimitStatus(clientId));
    }
}
