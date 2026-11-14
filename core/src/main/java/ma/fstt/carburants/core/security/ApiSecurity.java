package ma.fstt.carburants.core.security;

import jakarta.enterprise.context.ApplicationScoped;
import ma.fstt.carburants.api.dto.TokenResponse;
import ma.fstt.carburants.api.security.BasicCredentials;
import ma.fstt.carburants.api.security.CredentialStore;
import ma.fstt.carburants.api.security.JwtService;
import ma.fstt.carburants.api.security.LoginRateLimiter;

import java.time.Clock;

/**
 * Wires the pure security classes of the api module from configuration. One instance per web
 * application (each WAR has its own credentials store and login throttle, which is enough here).
 *
 * <p>Initialisation is lazy so that a missing secret produces a clear error at the first secured call
 * (and in the server log) instead of preventing the whole deployment.
 */
@ApplicationScoped
public class ApiSecurity {

    private static final int MAX_FAILURES = 5;
    private static final long LOCK_WINDOW_SECONDS = 300;

    private JwtService jwt;
    private CredentialStore credentials;

    private synchronized void init() {
        if (jwt != null) {
            return;
        }
        String secret = AppConfig.require(AppConfig.JWT_SECRET);
        long ttl = Long.parseLong(AppConfig.get(AppConfig.JWT_TTL, "900"));
        String user = AppConfig.get(AppConfig.API_USER, "admin");
        String password = AppConfig.require(AppConfig.API_PASSWORD);

        LoginRateLimiter limiter = new LoginRateLimiter(MAX_FAILURES, LOCK_WINDOW_SECONDS, Clock.systemUTC());
        CredentialStore store = new CredentialStore(limiter).addPlain(user, password);
        this.credentials = store;
        this.jwt = new JwtService(secret, ttl);
    }

    private void ensureInit() {
        if (jwt == null) {
            init();
        }
    }

    /** Checks the credentials and issues a JWT. Used by the REST login endpoints. */
    public TokenResponse login(String username, String password) {
        ensureInit();
        credentials.authenticate(username, password);
        return new TokenResponse(jwt.issue(username), jwt.getTtlSeconds());
    }

    /** Verifies "Authorization: Bearer ..." and returns the user name. Used by the REST filter. */
    public String verifyBearer(String authorizationHeader) {
        ensureInit();
        return jwt.verifyBearerHeader(authorizationHeader);
    }

    /** Verifies "Authorization: Basic ..." and returns the user name. Used by the SOAP filter. */
    public String verifyBasic(String authorizationHeader) {
        ensureInit();
        BasicCredentials c = BasicCredentials.parse(authorizationHeader);
        credentials.authenticate(c.getUsername(), c.getPassword());
        return c.getUsername();
    }
}
