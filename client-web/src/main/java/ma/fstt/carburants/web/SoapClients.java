package ma.fstt.carburants.web;

import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.handler.MessageContext;
import ma.fstt.carburants.api.soap.CarburantsSoapService;

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Builds JAX-WS client proxies for the SOAP backend.
 *
 * <p>The {@link Service} (which downloads and parses the public WSDL) is created once and reused;
 * the port (the proxy) is created per call because ports are not guaranteed thread-safe.
 * The endpoint URL comes from the system property {@code carburants.soap.url}
 * (default: http://localhost:8081/carburants-soap/CarburantsService).
 */
public final class SoapClients {

    static final String NAMESPACE = "http://carburants.fstt.ma/soap";
    private static final String DEFAULT_URL = "http://localhost:8081/carburants-soap/CarburantsService";

    private static Service service;

    private SoapClients() {
    }

    static String endpointUrl() {
        String value = System.getProperty("carburants.soap.url");
        return value == null || value.isBlank() ? DEFAULT_URL : value.trim();
    }

    private static synchronized Service service() throws MalformedURLException {
        if (service == null) {
            service = Service.create(new URL(endpointUrl() + "?wsdl"), new QName(NAMESPACE, "CarburantsService"));
        }
        return service;
    }

    /** Builds the value of the Authorization header from a user name and a password. */
    public static String basicHeader(String username, String password) {
        String raw = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates a proxy that sends the given Authorization header on every call.
     *
     * @param authorization value built by {@link #basicHeader(String, String)}
     */
    public static CarburantsSoapService port(String authorization) {
        try {
            CarburantsSoapService port = service().getPort(new QName(NAMESPACE, "CarburantsSoapPort"), CarburantsSoapService.class);
            BindingProvider provider = (BindingProvider) port;
            // The address in the WSDL is the one the server sees; use the configured one instead.
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointUrl());
            Map<String, List<String>> headers = Collections.singletonMap("Authorization", Collections.singletonList(authorization));
            provider.getRequestContext().put(MessageContext.HTTP_REQUEST_HEADERS, headers);
            return port;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid SOAP URL: " + endpointUrl(), e);
        }
    }

    /** Forgets the cached service, so the next call downloads the WSDL again (used after a failure). */
    static synchronized void reset() {
        service = null;
    }
}
