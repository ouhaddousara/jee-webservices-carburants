package ma.fstt.carburants.core;

import jakarta.enterprise.inject.spi.CDI;

/**
 * Programmatic CDI lookup used by the web service classes.
 *
 * <p>Decision: JAX-WS endpoints and JAX-RS resources (RESTEasy and Jersey) do not use {@code @Inject}.
 * Field injection into a JAX-RS resource depends on the JAX-RS runtime's CDI integration, and Jersey
 * embedded in a WAR on WildFly has its own injection (HK2) that can fight with Weld. Asking CDI
 * directly works the same way under all three runtimes, so the behaviour is identical.
 */
public final class ServiceLocator {

    private ServiceLocator() {
    }

    public static <T> T get(Class<T> type) {
        return CDI.current().select(type).get();
    }
}
