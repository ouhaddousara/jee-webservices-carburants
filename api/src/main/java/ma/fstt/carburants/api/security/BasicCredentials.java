package ma.fstt.carburants.api.security;

import ma.fstt.carburants.api.exception.BusinessException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Parser for the HTTP Basic "Authorization" header (RFC 7617), used by the SOAP service. */
public final class BasicCredentials {

    private final String username;
    private final String password;

    private BasicCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    /** @throws BusinessException UNAUTHORIZED if the header is missing or malformed */
    public static BasicCredentials parse(String header) {
        if (header == null || !header.regionMatches(true, 0, "Basic ", 0, 6)) {
            throw BusinessException.unauthorized("Authentification requise : en-tête Authorization: Basic.");
        }
        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(header.substring(6).trim()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw BusinessException.unauthorized("Identifiants illisibles.");
        }
        int colon = decoded.indexOf(':');
        if (colon <= 0) {
            throw BusinessException.unauthorized("Identifiants illisibles.");
        }
        return new BasicCredentials(decoded.substring(0, colon), decoded.substring(colon + 1));
    }
}
