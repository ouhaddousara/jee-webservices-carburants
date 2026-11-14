package ma.fstt.carburants.api.exception;

/**
 * A business rule was violated. The message is written for the end user (French) and never contains
 * SQL text or stack traces: every entry layer (SOAP fault, HTTP status, JSP page) can show it as is.
 *
 * <p>Unchecked on purpose: when it crosses a container-managed transaction boundary it triggers the
 * rollback and reaches the entry layer unwrapped.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Error category; each category maps to one HTTP status in the REST layer. */
    public enum Code {
        VALIDATION(400),
        UNAUTHORIZED(401),
        NOT_FOUND(404),
        CONFLICT(409),
        TOO_MANY_REQUESTS(429);

        private final int httpStatus;

        Code(int httpStatus) {
            this.httpStatus = httpStatus;
        }

        public int httpStatus() {
            return httpStatus;
        }
    }

    private final Code code;

    public BusinessException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public Code getCode() {
        return code;
    }

    public int getHttpStatus() {
        return code.httpStatus();
    }

    public static BusinessException validation(String message) {
        return new BusinessException(Code.VALIDATION, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(Code.NOT_FOUND, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(Code.CONFLICT, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(Code.UNAUTHORIZED, message);
    }

    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(Code.TOO_MANY_REQUESTS, message);
    }
}
