package ma.fstt.carburants.api.security;

import ma.fstt.carburants.api.exception.BusinessException;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Throttles failed logins per key (user name): after {@code maxFailures} consecutive failures inside
 * the window, further attempts are refused (HTTP 429) until the window expires. A success resets the key.
 *
 * <p>In memory and per JVM: enough for a single WildFly instance. A cluster would need a shared store.
 */
public class LoginRateLimiter {

    private static final class Entry {
        int failures;
        long windowStart;
    }

    private final int maxFailures;
    private final long windowSeconds;
    private final Clock clock;
    private final ConcurrentMap<String, Entry> entries = new ConcurrentHashMap<>();

    public LoginRateLimiter(int maxFailures, long windowSeconds, Clock clock) {
        this.maxFailures = maxFailures;
        this.windowSeconds = windowSeconds;
        this.clock = clock;
    }

    /** Fails with 429 if the key is currently locked. Call BEFORE checking the password. */
    public void checkAllowed(String key) {
        Entry e = entries.get(key);
        if (e == null) {
            return;
        }
        synchronized (e) {
            if (now() - e.windowStart >= windowSeconds) {
                entries.remove(key, e);
                return;
            }
            if (e.failures >= maxFailures) {
                throw BusinessException.tooManyRequests("Trop de tentatives. Réessayez dans quelques minutes.");
            }
        }
    }

    public void recordFailure(String key) {
        Entry e = entries.computeIfAbsent(key, k -> {
            Entry fresh = new Entry();
            fresh.windowStart = now();
            return fresh;
        });
        synchronized (e) {
            if (now() - e.windowStart >= windowSeconds) {
                e.failures = 0;
                e.windowStart = now();
            }
            e.failures++;
        }
    }

    public void recordSuccess(String key) {
        entries.remove(key);
    }

    private long now() {
        return clock.instant().getEpochSecond();
    }
}
