package ma.fstt.carburants.soap;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ma.fstt.carburants.api.exception.BusinessException;
import ma.fstt.carburants.core.ServiceLocator;
import ma.fstt.carburants.core.security.ApiSecurity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HTTP Basic authentication for the SOAP endpoint.
 *
 * <p>Decision: a servlet filter in front of the endpoint instead of reading headers inside the
 * JAX-WS code. It does not depend on the SOAP engine's internals, works for any client (Postman,
 * SoapUI, JAX-WS proxy) and answers a real HTTP 401 with a {@code WWW-Authenticate} challenge.
 * The WSDL itself ({@code ?wsdl}, {@code ?xsd=...}) stays public: the contract is not a secret,
 * the operations are.
 */
@WebFilter(urlPatterns = "/CarburantsService")
public class SoapBasicAuthFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(SoapBasicAuthFilter.class.getName());

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (isContractRequest(request)) {
            chain.doFilter(req, res);
            return;
        }
        try {
            ServiceLocator.get(ApiSecurity.class).verifyBasic(request.getHeader("Authorization"));
        } catch (BusinessException e) {
            reject(response, e.getHttpStatus(), e.getMessage());
            return;
        } catch (RuntimeException e) {
            // Typically a missing configuration variable: logged for the developer, hidden from the client.
            LOG.log(Level.SEVERE, "SOAP authentication could not be performed", e);
            reject(response, 500, "Erreur interne du service.");
            return;
        }
        chain.doFilter(req, res);
    }

    private static boolean isContractRequest(HttpServletRequest request) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String query = request.getQueryString();
        return query != null && (query.equalsIgnoreCase("wsdl") || query.startsWith("xsd=")
                || query.startsWith("wsdl=") || query.startsWith("WSDL"));
    }

    private static void reject(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        if (status == 401) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"carburants-soap\", charset=\"UTF-8\"");
        }
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setContentType("text/plain;charset=UTF-8");
        response.getOutputStream().write(message.getBytes(StandardCharsets.UTF_8));
    }
}
