package ma.fstt.carburants.core.security;

/**
 * Reads configuration from JVM system properties first, then from environment variables.
 * Secrets are never written in the code, in a committed file or in standalone.xml: they are defined
 * in the shell that starts WildFly (see the guide).
 */
public final class AppConfig {

    /** HMAC key of the JWT; at least 32 characters. */
    public static final String JWT_SECRET = "CARBURANTS_JWT_SECRET";
    /** Lifetime of a token, in seconds (default 900). */
    public static final String JWT_TTL = "CARBURANTS_JWT_TTL";
    /** The API user name (default "admin"). */
    public static final String API_USER = "CARBURANTS_API_USER";
    /** The API user password; hashed at startup with PBKDF2 and never kept in clear. */
    public static final String API_PASSWORD = "CARBURANTS_API_PASSWORD";

    private AppConfig() {
    }

    /** Returns the value, or {@code defaultValue} when it is missing or blank. */
    public static String get(String name, String defaultValue) {
        String value = System.getProperty(name);
        if (value == null || value.isBlank()) {
            value = System.getenv(name);
        }
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    /** Returns the value or fails with a message that tells the developer exactly what to define. */
    public static String require(String name) {
        String value = get(name, null);
        if (value == null) {
            throw new IllegalStateException("Missing configuration: define the environment variable " + name
                    + " in the shell that starts WildFly (see the guide, section on secrets).");
        }
        return value;
    }
}
