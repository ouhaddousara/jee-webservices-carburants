package ma.fstt.carburants.rest;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import ma.fstt.carburants.api.dto.LoginRequest;
import ma.fstt.carburants.api.dto.TokenResponse;
import ma.fstt.carburants.core.ServiceLocator;
import ma.fstt.carburants.core.security.ApiSecurity;

/** Token endpoint: POST /auth/login with {"username": "...", "password": "..."} returns a JWT. */
@Path("/auth")
public class AuthResource {

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TokenResponse login(LoginRequest request) {
        String user = request == null ? null : request.getUsername();
        String password = request == null ? null : request.getPassword();
        return ServiceLocator.get(ApiSecurity.class).login(user, password);
    }
}
