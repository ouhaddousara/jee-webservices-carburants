package ma.fstt.carburants.api.security;

import ma.fstt.carburants.api.exception.BusinessException;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds the accepted API users as PBKDF2 hashes and authenticates them, throttled by a
 * {@link LoginRateLimiter}. Credentials come from configuration (see {@code AppConfig} in rest-common
 * and soap), never from source code.
 */
public class CredentialStore {

    /** Hash checked when the user is unknown, so the response time does not reveal valid user names. */
    private static final String DUMMY_HASH = PasswordHasher.hash("dummy-password-for-timing");

    private final Map<String, String> hashesByUser = new HashMap<>();
    private final LoginRateLimiter limiter;

    public CredentialStore(LoginRateLimiter limiter) {
        this.limiter = limiter;
    }

    /** Registers a user whose password is already hashed ("pbkdf2$..." format). */
    public CredentialStore addHashed(String username, String storedHash) {
        hashesByUser.put(username, storedHash);
        return this;
    }

    /** Registers a user from a clear password (hashed immediately, never kept). */
    public CredentialStore addPlain(String username, String password) {
        hashesByUser.put(username, PasswordHasher.hash(password));
        return this;
    }

    /**
     * Checks the credentials.
     *
     * @throws BusinessException UNAUTHORIZED (401) for bad credentials, TOO_MANY_REQUESTS (429) if locked
     */
    public void authenticate(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            throw BusinessException.unauthorized("Identifiants invalides.");
        }
        limiter.checkAllowed(username);
        String stored = hashesByUser.get(username);
        boolean ok = PasswordHasher.matches(password, stored != null ? stored : DUMMY_HASH) && stored != null;
        if (!ok) {
            limiter.recordFailure(username);
            throw BusinessException.unauthorized("Identifiants invalides.");
        }
        limiter.recordSuccess(username);
    }
}
