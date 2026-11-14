package ma.fstt.carburants.api.dto;

import java.io.Serializable;

/** Answer of POST /auth/login: a bearer JWT and its lifetime. */
public class TokenResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private String tokenType = "Bearer";
    private long expiresInSeconds;

    public TokenResponse() {
    }

    public TokenResponse(String token, long expiresInSeconds) {
        this.token = token;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }
}
