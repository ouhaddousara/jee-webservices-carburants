package ma.fstt.carburants.restapp;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import ma.fstt.carburants.rest.CarburantsRestClasses;

import java.util.Set;

/**
 * JAX-RS entry point of the "standard" service. WildFly detects this class and serves it with
 * RESTEasy, its built-in implementation: no JAX-RS library is embedded in this WAR.
 * URL base: http://localhost:8081/carburants-rest/api
 */
@ApplicationPath("/api")
public class RestApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        return CarburantsRestClasses.classes();
    }
}
