package ma.fstt.carburants.jerseyapp;

import jakarta.ws.rs.ApplicationPath;
import ma.fstt.carburants.rest.CarburantsRestClasses;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * JAX-RS entry point of the "Jersey" service: Jersey's own {@link ResourceConfig}, served by the
 * Jersey servlet container embedded in this WAR (auto-registered, no web.xml needed).
 * URL base: http://localhost:8081/carburants-jersey/api
 */
@ApplicationPath("/api")
public class JerseyApplication extends ResourceConfig {

    public JerseyApplication() {
        registerClasses(CarburantsRestClasses.classes());
        // JSON through Jackson, explicitly (instead of relying on auto-discovery).
        register(JacksonFeature.class);
    }
}
