package ma.fstt.carburants.rest;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ma.fstt.carburants.api.dto.ErrorDto;
import ma.fstt.carburants.api.exception.BusinessException;

/** Maps a business error to its HTTP status (400, 401, 404, 409, 429) and a JSON body. */
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    @Override
    public Response toResponse(BusinessException e) {
        Response.ResponseBuilder builder = Response.status(e.getHttpStatus())
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorDto(e.getCode().name(), e.getMessage()));
        if (e.getHttpStatus() == 401) {
            builder.header("WWW-Authenticate", "Bearer realm=\"carburants\"");
        }
        return builder.build();
    }
}
