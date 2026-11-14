package ma.fstt.carburants.api.dto;

import java.io.Serializable;

/** JSON error body of the REST services: a stable code and a message safe to show to a user. */
public class ErrorDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String code;
    private String message;

    public ErrorDto() {
    }

    public ErrorDto(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
