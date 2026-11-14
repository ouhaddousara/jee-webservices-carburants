package ma.fstt.carburants.api.security;

import ma.fstt.carburants.api.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Minimal JWT (RFC 7519) implementation: HS256 signature, "sub", "iat" and "exp" claims.
 *
 * <p>Written with the JDK only (no third-party library) so the same class runs in the three WARs and
 * in the pure-logic tests. Decisions: the algorithm is fixed to HS256 and the header is never trusted
 * to select it (this blocks the classic "alg=none" and algorithm-confusion attacks); the signature is
 * compared in constant time; expiry is checked with an injectable clock so it can be tested.
 */
public class JwtService {

    private static final Base64.Encoder B64_ENC = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64_DEC = Base64.getUrlDecoder();
    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String HEADER_B64 = B64_ENC.encodeToString(HEADER_JSON.getBytes(StandardCharsets.UTF_8));
    // The value may contain escaped characters (\" and \\), so match "any non-quote/backslash char or an escape pair".
    private static final Pattern SUB = Pattern.compile("\"sub\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
    private static final Pattern EXP = Pattern.compile("\"exp\"\\s*:\\s*(\\d+)");

    private final byte[] secret;
    private final long ttlSeconds;
    private final Clock clock;

    public JwtService(String secret, long ttlSeconds) {
        this(secret, ttlSeconds, Clock.systemUTC());
    }

    public JwtService(String secret, long ttlSeconds, Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("The JWT secret must contain at least 32 bytes.");
        }
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("The JWT lifetime must be positive.");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
        this.clock = clock;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    /** Issues a signed token for the given user. */
    public String issue(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("A token needs a user name.");
        }
        long now = clock.instant().getEpochSecond();
        String payloadJson = "{\"sub\":\"" + escape(username) + "\",\"iat\":" + now + ",\"exp\":" + (now + ttlSeconds) + "}";
        String payload = B64_ENC.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signingInput = HEADER_B64 + "." + payload;
        return signingInput + "." + sign(signingInput);
    }

    /**
     * Verifies the token and returns the user name it was issued for.
     *
     * @throws BusinessException UNAUTHORIZED (401) if the token is malformed, forged or expired
     */
    public String verify(String token) {
        if (token == null || token.isBlank()) {
            throw BusinessException.unauthorized("Jeton manquant.");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw BusinessException.unauthorized("Jeton invalide.");
        }
        // The header must be exactly the one this service issues: no alg negotiation.
        if (!MessageDigest.isEqual(parts[0].getBytes(StandardCharsets.UTF_8), HEADER_B64.getBytes(StandardCharsets.UTF_8))) {
            throw BusinessException.unauthorized("Jeton invalide.");
        }
        String expected = sign(parts[0] + "." + parts[1]);
        if (!MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw BusinessException.unauthorized("Jeton invalide.");
        }
        String payloadJson;
        try {
            payloadJson = new String(B64_DEC.decode(parts[1]), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw BusinessException.unauthorized("Jeton invalide.");
        }
        Matcher exp = EXP.matcher(payloadJson);
        Matcher sub = SUB.matcher(payloadJson);
        if (!exp.find() || !sub.find()) {
            throw BusinessException.unauthorized("Jeton invalide.");
        }
        long expiry;
        try {
            expiry = Long.parseLong(exp.group(1));
        } catch (NumberFormatException e) {
            throw BusinessException.unauthorized("Jeton invalide.");
        }
        if (clock.instant().getEpochSecond() >= expiry) {
            throw BusinessException.unauthorized("Jeton expiré.");
        }
        return unescape(sub.group(1));
    }

    /** Extracts the token from an "Authorization: Bearer xxx" header value, or fails with 401. */
    public String verifyBearerHeader(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw BusinessException.unauthorized("Authentification requise : en-tête Authorization: Bearer <jeton>.");
        }
        return verify(authorizationHeader.substring(7).trim());
    }

    private String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return B64_ENC.encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HmacSHA256 is not available.", e);
        }
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String unescape(String s) {
        return s.replace("\\\"", "\"").replace("\\\\", "\\");
    }
}
