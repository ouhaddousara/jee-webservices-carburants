package ma.fstt.carburants.rest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import ma.fstt.carburants.core.ServiceLocator;
import ma.fstt.carburants.core.security.ApiSecurity;

import java.io.IOException;

/**
 * Rejects every {@link Secured} request that has no valid bearer token. A {@code BusinessException}
 * (401) thrown here is turned into a JSON answer by {@link BusinessExceptionMapper}.
 */
@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext request) throws IOException {
        String header = request.getHeaderString(HttpHeaders.AUTHORIZATION);
        ServiceLocator.get(ApiSecurity.class).verifyBearer(header);
    }
}
