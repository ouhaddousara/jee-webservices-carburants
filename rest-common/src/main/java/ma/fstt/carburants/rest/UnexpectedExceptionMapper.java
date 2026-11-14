package ma.fstt.carburants.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import ma.fstt.carburants.api.dto.ErrorDto;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Last line of defence: anything that is not a business error becomes a generic 500. The cause goes
 * to the server log only, never to the client (no SQL, no stack trace, no class names).
 */
@Provider
public class UnexpectedExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOG = Logger.getLogger(UnexpectedExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable t) {
        if (t instanceof WebApplicationException) {
            // 404 for an unknown path, 405, 415, malformed request... keep the status chosen by the runtime.
            WebApplicationException wae = (WebApplicationException) t;
            int status = wae.getResponse().getStatus();
            return Response.status(status).type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorDto("HTTP_" + status, "Requête non valide ou ressource inconnue.")).build();
        }
        LOG.log(Level.SEVERE, "Unexpected error in a REST call", t);
        return Response.status(500).type(MediaType.APPLICATION_JSON)
                .entity(new ErrorDto("INTERNAL_ERROR", "Erreur interne du service.")).build();
    }
}
