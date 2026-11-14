package ma.fstt.carburants.rest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * The explicit list of JAX-RS classes of the service. Both Application subclasses (RESTEasy and Jersey)
 * return exactly this set, so the two services expose the same API through different runtimes.
 * An explicit list also avoids classpath scanning of the embedded Jersey jars.
 */
public final class CarburantsRestClasses {

    private CarburantsRestClasses() {
    }

    public static Set<Class<?>> classes() {
        Set<Class<?>> set = new LinkedHashSet<>();
        set.add(AuthResource.class);
        set.add(PrixResource.class);
        set.add(JwtAuthFilter.class);
        set.add(SecurityHeadersFilter.class);
        set.add(BusinessExceptionMapper.class);
        set.add(UnexpectedExceptionMapper.class);
        return Collections.unmodifiableSet(set);
    }
}
